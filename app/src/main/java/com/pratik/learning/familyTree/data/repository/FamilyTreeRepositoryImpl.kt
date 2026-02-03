package com.pratik.learning.familyTree.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pratik.learning.familyTree.data.local.dao.FamilyTreeDao
import com.pratik.learning.familyTree.data.local.dto.AncestorNode
import com.pratik.learning.familyTree.data.local.dto.ChildWithSpouseDto
import com.pratik.learning.familyTree.data.local.dto.DescendantNode
import com.pratik.learning.familyTree.data.local.dto.DualAncestorTree
import com.pratik.learning.familyTree.data.local.dto.FamilyMember
import com.pratik.learning.familyTree.data.local.dto.FamilyRelation
import com.pratik.learning.familyTree.data.local.dto.FullFamilyTree
import com.pratik.learning.familyTree.data.local.dto.MemberRelationAR
import com.pratik.learning.familyTree.data.local.dto.MemberWithFather
import com.pratik.learning.familyTree.data.local.dto.TimelineEvent
import com.pratik.learning.familyTree.data.local.dto.TimelineEventType
import com.pratik.learning.familyTree.data.local.model.MemberFilter
import com.pratik.learning.familyTree.data.local.model.RelationContext
import com.pratik.learning.familyTree.utils.DATA_BACKUP_FILE_NAME
import com.pratik.learning.familyTree.utils.DATA_BACKUP_FILE_PATH
import com.pratik.learning.familyTree.utils.GENDER_TYPE_FEMALE
import com.pratik.learning.familyTree.utils.GENDER_TYPE_MALE
import com.pratik.learning.familyTree.utils.HiText
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER_OF_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER_OF_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER_OF_WIFI
import com.pratik.learning.familyTree.utils.RELATION_TYPE_COUSIN_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_COUSIN_BROTHER_FATHER_SIDE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_COUSIN_BROTHER_MOTHER_SIDE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_COUSIN_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_COUSIN_SISTER_FATHER_SIDE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_COUSIN_SISTER_MOTHER_SIDE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER_OF_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER_OF_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_ELDER_BROTHER_OF_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER_IN_LAW
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDCHILD_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDCHILD_MOTHER_SIDE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDCHILD_MOTHER_SIDE_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDFATHER_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDFATHER_M
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDMOTHER_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDMOTHER_M
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GREAT_GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GREAT_GREAT_GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GREAT____GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND_OF_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND_OF_SISTER_OF_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND_OF_SISTER_OF_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER_IN_LAW
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER_OF_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER_OF_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER_OF_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER_OF_WIFI
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON_OF_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON_OF_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE_OF_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE_OF_BROTHER_OF_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE_OF_ELDER_BROTHER_OF_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE_OF_YOUNGER_BROTHER_OF_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_YOUNGER_BROTHER_OF_FATHER
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.setLastSyncTime
import com.pratik.learning.familyTree.utils.calculateAgeFromDob
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.relationTextInHindi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.plus

class FamilyTreeRepositoryImpl(
    private val dao: FamilyTreeDao,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
) : FamilyTreeRepository {
    // Member Flow for UI updates
    override val allMembers: Flow<List<FamilyMember>> = dao.getAllMembers()

    override suspend fun insertAllMembers(members: List<FamilyMember>) {
        dao.insertAllMembers(members)
    }

    override suspend fun insertMember(member: FamilyMember): Long {
        member.memberId = (dao.getMaxMemberId() ?: 0) + 1
        logger("insertMember: $member")
        return dao.insertMember(member)
    }

    override suspend fun updateMember(
        member: FamilyMember,
        isGotraChanged: Boolean,
        spouseId: Int
    ) {
        logger("updateMember: $member  isGotraChanged = $isGotraChanged  spouseId = $spouseId")
        dao.updateGotra(member.memberId, member.gotra)
        if (spouseId != -1) {
            logger("updateMember: spouseId = $spouseId")
            dao.updateGotra(spouseId, member.gotra)
        }
        updateDescendantsGotra(member.memberId, member.gotra)

        dao.updateMember(member)
    }

    override suspend fun deleteMember(id: Int) {
        logger("deleteMember: $id")
        dao.deleteMemberWithRelations(id)
    }

    override suspend fun deleteAllRelations(id: Int) {
        logger("deleteAllRelations: $id")
        dao.deleteRelationsForMember(id)
    }

    // Relation operations
    override suspend fun getRelationsForMember(memberId: Int): Flow<List<FamilyRelation>> {
        logger("getRelationsForMember: $memberId")
        return dao.getRelationsForMember(memberId)
    }

    override suspend fun insertRelation(relation: FamilyRelation) {
        logger("insertRelation: $relation")
        dao.insertRelation(relation)
    }

    override suspend fun insertAllRelation(relation: List<FamilyRelation>) {
        logger("insertAllRelation: $relation")
        dao.insertAllRelations(relation)
    }

    override suspend fun deleteRelation(m1Id: Int, m2Id: Int, type: String) {
        logger("deleteRelation: $m1Id, $m2Id, $type")
        dao.deleteRelation(m1Id, m2Id, type)
    }

    override suspend fun getMemberById(id: Int): FamilyMember? {
        return dao.getMemberById(id)
    }

    override fun getPagedMembersForSearchByName(
        name: String,
        isUnmarried: Boolean
    ): Flow<PagingData<MemberWithFather>> {
        logger("getPagedMembersForSearchByName Loading page with matched: $name")
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getAllMembersBySearchQuery(name, isUnmarried) }
        ).flow
    }

    override fun getPagedMembersForSearchByFilter(
        name: String,
        filterMatrix: MemberFilter
    ): Flow<PagingData<MemberWithFather>> {
        logger("getPagedMembersForSearchByFilter Loading page with matched: $name")
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getAllMembersBySearchQuery(name, filterMatrix.isUnmarried, filterMatrix.sortBy) }
        ).flow
    }

    /**
     * to update the Gotra of all the unmarried or male members when elderly gotra is updated
     * */
    private suspend fun updateDescendantsGotra(memberId: Int, newGotra: String) {
        dao.getChildrenWithSpouse(memberId).forEach { child ->
            if (child.spouseId == null || child.spouseId == -1) {
                logger("updateDescendantsGotra:: updating gotra for ${child.child.fullName} , newGotra = $newGotra")
                updateDescendantsGotra(child.child.memberId, newGotra)
                dao.updateGotra(child.child.memberId, newGotra)
            } else {
                if (child.child.gender == GENDER_TYPE_MALE) {
                    logger("updateDescendantsGotra:: updating gotra for ${child.child.fullName}  || spouse = ${child.spouseFullName}, newGotra = $newGotra")
                    updateDescendantsGotra(child.child.memberId, newGotra)
                    dao.updateGotra(child.child.memberId, newGotra)
                    dao.updateGotra(child.spouseId, newGotra)
                }
            }
        }
    }

    // The complex, derived relationship logic (like reciprocal and inferred relations)
    // should live here or in a separate 'TreeLogicManager' service, not in the DAO.

    private suspend fun buildAncestryNode(
        member: FamilyMember,
        level: Int,
        familyTreeDao: FamilyTreeDao,
        isParental: Boolean = true
    ): AncestorNode {
        logger("buildAncestryNode:: called for member = ${member.fullName},  isParental = $isParental")
        // 1. Get Spouse using getSpouse()
        val spouse = familyTreeDao.getSpouse(member.memberId)

        // 2. Recursively find Father's line
        val father = familyTreeDao.getImmediateParentByType(member.memberId, "Father")
        val fatherNode =
            father?.let { buildAncestryNode(it, level = level + 1, familyTreeDao, isParental) }

        // 3. Recursively find Mother's line
        val mother = familyTreeDao.getImmediateParentByType(member.memberId, "Mother")
        val motherNode =
            mother?.let { buildAncestryNode(it, level = level + 1, familyTreeDao, isParental) }
        logger("buildAncestryNode:: member = ${member.fullName}, father = $fatherNode, mother = $motherNode  isParental = $isParental")

        // 4. Construct the AncestorNode
        val parents = mutableListOf<AncestorNode>()
        if (fatherNode != null) parents.add(fatherNode)
        if (motherNode != null) parents.add(motherNode)

        return AncestorNode(
            member = member,
            spouse = spouse,
            parents = parents,
            level = level + 1,
            relationWithMember = getRelationWithMember(level + 1, isParental)
        )
    }

    override suspend fun getFullAncestorTree(memberId: Int): DualAncestorTree? {
        // 1. Get the starting member (e.g., Pratik)
        val startingMember = dao.getMemberById(memberId) ?: return null

        // 2. Find the root of the paternal line
        val father = dao.getImmediateParentByType(startingMember.memberId, "Father")

        // 3. Find the root of the maternal line
        val mother = dao.getImmediateParentByType(startingMember.memberId, "Mother")

        // 4. Build the full tree recursively for each line
        val paternalTree = father?.let { buildAncestryNode(it, level = 1, dao, isParental = true) }
        val maternalTree = mother?.let { buildAncestryNode(it, level = 1, dao, isParental = false) }

        // 5. Find the current couple
        val currentSpouse = dao.getSpouse(startingMember.memberId)

        return DualAncestorTree(
            self = startingMember,
            spouse = currentSpouse,
            paternalLineRoot = paternalTree,
            maternalLineRoot = maternalTree
        )
    }

    override suspend fun getCompleteFamilyTree(memberId: Int): FullFamilyTree? {
        val member = dao.getMemberById(memberId) ?: return null

        // Spouse
        val spouse = dao.getSpouse(memberId)

        // Build both ancestor and descendant trees
        val ancestors = getFullAncestorTree(memberId)
        val descendants = getFullDescendantTree(memberId)

        return FullFamilyTree(
            self = member,
            spouse = spouse,
            ancestors = ancestors,
            descendants = descendants
        )
    }

    override suspend fun getFullDescendantTree(memberId: Int): DescendantNode? {
        // 1. Get the starting member
        val startingMember = dao.getMemberById(memberId) ?: return null

        // 2. Build the descendant tree from this member
        return buildDescendantNode(startingMember, level = 1, familyTreeDao = dao)
    }

    private suspend fun buildDescendantNode(
        member: FamilyMember,
        level: Int,
        familyTreeDao: FamilyTreeDao
    ): DescendantNode {
        logger("buildDescendantNode:: called for member = ${member.fullName}")

        val spouse = familyTreeDao.getSpouse(member.memberId)

        // Fetch all children
        val children = familyTreeDao.getChildren(member.memberId)
        logger("buildDescendantNode:: member = ${member.fullName}, children = $children")

        // Recursively build descendant nodes
        val childNodes = children?.map { child ->
            buildDescendantNode(child, level + 1, familyTreeDao)
        }

        return DescendantNode(
            member = member,
            spouse = spouse,
            children = childNodes ?: emptyList(),
            level = level,
            relationWithMember = getDescendantRelationWithMember(
                level,
                member.gender == GENDER_TYPE_MALE
            )
        )
    }

    private fun getRelationWithMember(level: Int, isParental: Boolean): String {
        logger("getRelationWithMember:: level = $level, isParental = $isParental")
        return when (level) {
            1 -> ""
            2 -> "पिता - माता"
            3 -> if (isParental) "दादा जी - दादी जी" else "नाना जी : नानी जी"
            4 -> if (isParental) "परदादा जी - परदादी जी" else "परनाना जी : परनानी जी"
            else -> if (isParental) "पर...परदादा जी - पर...परदादी जी" else "पर...परनाना जी : पर...परनानी जी"
        }
    }

    private fun getDescendantRelationWithMember(level: Int, isSon: Boolean): String {
        return when (level) {
            1 -> ""
            2 -> if (isSon) "पुत्र-बहू" else "पुत्री-दामाद"
            3 -> if (isSon) RELATION_TYPE_GRANDCHILD else "नवासा/नवासी"
            4 -> if (isSon) RELATION_TYPE_GREAT_GRANDCHILD else "प्रपौत्र/प्रपौत्री"
            5 -> if (isSon) RELATION_TYPE_GREAT_GREAT_GRANDCHILD else "प्रपौत्र/प्रपौत्री"
            else -> if (isSon) RELATION_TYPE_GREAT____GRANDCHILD else "प्रपौत्र/प्रपौत्री"
        }.inHindi()
    }

    override suspend fun getParentsWithMemberId(memberId: Int): List<Pair<String, FamilyMember>> {
        logger("getParentsWithMemberId:: memberId = $memberId")
        val parents = mutableListOf<Pair<String, FamilyMember>>()
        val father = dao.getImmediateParentByType(memberId, RELATION_TYPE_FATHER)
        father?.let {
            parents.add(Pair("Father", father))
        }
        val mother = dao.getImmediateParentByType(memberId, RELATION_TYPE_MOTHER)
        mother?.let {
            parents.add(Pair("Mother", mother))
        }
        return parents
    }

    override suspend fun getSpouse(memberId: Int): FamilyMember? {
        logger("getSpouse:: memberId = $memberId")
        return dao.getSpouse(memberId)
    }

    override suspend fun getChildren(memberId: Int): List<FamilyMember> {
        logger("getChildren:: memberId = $memberId")
        return dao.getChildren(memberId) ?: emptyList()
    }

    override suspend fun getChildrenWithSpouse(memberId: Int): List<ChildWithSpouseDto> {
        logger("getChildrenWithSpouse:: memberId = $memberId")
        return dao.getChildrenWithSpouse(memberId)
    }

    override suspend fun downloadDataFromServer(): Boolean {
        logger("loadLocalDBFromServer:: Started fetching data from Server")
        try {
            val gson = Gson()
            // 1. Download JSON file
            val ref = storage.reference.child(DATA_BACKUP_FILE_PATH)
            val bytes = ref.getBytes(10 * 1024 * 1024).await() // up to 10MB
            val json = String(bytes)


            // 2. Parse JSON into data classes
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val jsonObject = gson.fromJson<Map<String, Any>>(json, type)


            val membersJson = gson.toJson(jsonObject["familyTree_members"])
            val relationsJson = gson.toJson(jsonObject["familyTree_relations"])

            logger("loadLocalDBFromServer:: Successfully fetched membersJson = $membersJson, relationsJson = $relationsJson")
            val members = gson.fromJson(membersJson, Array<FamilyMember>::class.java).toList()
            val relations = gson.fromJson(relationsJson, Array<FamilyRelation>::class.java).toList()
            logger("loadLocalDBFromServer:: Successfully fetched memberIds= ${members.map { it.memberId }}, relationsJson = $relationsJson")

//            dao.clearMembers()
//            dao.clearRelations()

            logger("loadLocalDBFromServer:: Successfully fetched membersJson 32 = $members, relationsJson = $relations")
            // 3. Update Room database
            dao.insertAllMembersAndRelations(members = members, relations = relations)
            setLastSyncTime(context, System.currentTimeMillis())
            return true
        } catch (e: Exception) {
            Log.e("loadLocalDBFromServer", "Sync failed: ${e.message}", e)
            return false
        }
    }


    override suspend fun syncDataToFirebase(isAlsoDownload: Boolean) {
        logger("syncDataToFirebase Started syncing data to Firebase server, isAlsoDownload = $isAlsoDownload")
        withContext(Dispatchers.IO) {
            try {
                // Step 1: Fetch all local data
                val members = dao.getAllMembersForServer()
                val relations = dao.getAllRelationsForServer()

                logger("syncDataToFirebase:: syncDataToFirebase uploading ${members.size} members & ${relations.size} relations.")
                // Step 2: Prepare JSON
                val dataMap = mapOf(
                    "familyTree_members" to members,
                    "familyTree_relations" to relations
                )
                val jsonString = GsonBuilder().setPrettyPrinting().create().toJson(dataMap)

                // Step 3: Create temp file
                val tempFile = File(context.cacheDir, DATA_BACKUP_FILE_NAME)
                tempFile.writeText(jsonString)

                // Step 4: Upload to Firebase Storage
                val storageRef = storage.reference.child(DATA_BACKUP_FILE_PATH)
                val uploadTask = storageRef.putFile(Uri.fromFile(tempFile)).await()

                logger("syncDataToFirebase:: syncDataToFirebase ✅ Upload complete: ${uploadTask.metadata?.path}")

                // Step 5: Delete local temp file
                tempFile.delete()
                logger("syncDataToFirebase:: syncDataToFirebase 🧹 Temp file deleted")
                setIsDataUpdateRequired(context, false)
                if (isAlsoDownload) {
                    logger("syncDataToFirebase:: syncDataToFirebase 📥 Downloading data from server")
                    downloadDataFromServer()
                }

            } catch (e: Exception) {
                logger("syncDataToFirebase:: syncDataToFirebase ❌ Sync failed: ${e.message}")
            }
        }
    }

    override suspend fun verifyInternetAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://clients3.google.com/generate_204")
            val connection = url.openConnection() as? HttpURLConnection

            connection?.run {
                connectTimeout = 1500
                readTimeout = 1500
                requestMethod = "GET"
                connect()

                val isConnected = (responseCode == 204)
                logger(
                    "verifyInternetAccess",
                    "HTTP check response: $responseCode, success: $isConnected"
                )

                disconnect()
                return@withContext isConnected
            }

            logger("verifyInternetAccess:: Failed to open HTTP connection")
            return@withContext false
        } catch (e: Exception) {
            logger("verifyInternetAccess:: Exception: ${e.message}")
            return@withContext false
        }
    }

    override suspend fun isNoDataAndNoInternet(): Boolean {
        val memberCount = dao.getMemberCount()
        val hasInternet = verifyInternetAccess()
        logger("isNoDataAndNoInternet:: memberCount = $memberCount, hasInternet = $hasInternet")

        return memberCount == 0 && !hasInternet
    }

    override suspend fun getMemberRelatives(memberId: Int): MemberRelationAR {
        logger("getMemberRelatives:: memberId = $memberId")
        val relatives = MemberRelationAR()
        val relations = dao.getRelationsForMember(memberId).first()
        getRelations(memberId = memberId, relations = relations, relatives = relatives)
        relatives.member = getMemberById(memberId)
        logger("getMemberRelatives:: member 2 = ${relatives.member}")
        getMemberSmallBio(relatives)
        return relatives
    }


    /**
     * to get the relationship between two members
     * @param member1Relatives: MemberRelationAR info of all the relatives for member 1
     * @param member2Relatives: MemberRelationAR info of all the relatives for member 1
     * @return Pair of relation between two members in Hindi Pair of relation texts (Member1 → Member2, Member2 → Member1) in Hindi
     * */
    override suspend fun getMembersBetweenRelations(
        member1Relatives: MemberRelationAR,
        member2Relatives: MemberRelationAR
    ): Pair<String, String> {

        val m1 = member1Relatives.member ?: return "" to ""
        val m2 = member2Relatives.member ?: return "" to ""

        val relationContext = RelationContext(
            m1 = m1,
            m2 = m2,
            m1Rel = member1Relatives,
            m2Rel = member2Relatives
        )
        return findSiblingRelation(relationContext)
            ?: findParentChildRelation(relationContext)
            ?: findChildParentRelation(relationContext)
            ?: findGrandChildRelation(relationContext)
            ?: findGrandParentRelation(relationContext)
            ?: findCousinRelation(relationContext)
            ?: findFatherSideUncleAuntRelation(relationContext)
            ?: findFatherSideUncleAuntSpouseRelation(relationContext)
            ?: findFatherSideNephewNieceRelation(relationContext)
            ?: findFatherSideNephewNieceSpouseRelation(relationContext)
            ?: findMotherSideUncleAuntRelation(relationContext)
            ?: findMotherSideUncleAuntSpouseRelation(relationContext)
            ?: findMotherSideNephewNieceRelation(relationContext)
            ?: findMotherSideNephewNieceSpouseRelation(relationContext)
            ?: findJijaSalaBhabhiDevarOrNanandRelation(relationContext)
            ?: findSalaJijaDevarOrNanandBhabhiRelation(relationContext)
            ?: ("" to "")

    }

    /**
     * Checks sibling relationship.
     * (i.e., Member1's sibling == Member2 )
     * example: Member 1 (Payal Vishwakarma) (sister) -> Member 2 (Pratik) (brother)
     */
    private fun findSiblingRelation(ctx: RelationContext): Pair<String, String>? =
        ctx.m1Rel.siblings
            .firstOrNull { it.second.memberId == ctx.m2Id }
            ?.let { (relM2ToM1, _) ->
                val relM1ToM2 =
                    if (ctx.isM1Male) RELATION_TYPE_BROTHER else RELATION_TYPE_SISTER
                relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
            }

    /**
     * Member1 is parent of Member2.
     * (i.e., Member1's child == Member2 )
     * example: Member 1 (Kailash Vishwakarma) (Father) -> Member 2 (Pratik) (son)
     * example: Member 1 (Rajni Vishwakarma) (Mother) -> Member 2 (Pratik) (son)
     * example: Member 1 (Rajni Vishwakarma) (Mother) -> Member 2 (Payal) (daughter)
     */
    private fun findParentChildRelation(ctx: RelationContext): Pair<String, String>? =
        ctx.m1Rel.children
            .firstOrNull { it.second.child.memberId == ctx.m2Id }
            ?.let { (relM2ToM1, _) ->
                val relM1ToM2 =
                    if (ctx.isM1Male) RELATION_TYPE_FATHER else RELATION_TYPE_MOTHER
                relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
            }

    /**
     * Member1 is child of Member2.
     * (i.e., Member1's parent == Member2 )
     * example: Member 1 (Pratik) (son) -> Member 2 (Kailash Vishwakarma) (Father)
     * example: Member 1 (Pratik) (son) -> Member 2 (Rajni Vishwakarma) (Mother)
     * example: Member 1 (Payal) (daughter) -> Member 2 (Rajni Vishwakarma) (Mother)
     */
    private fun findChildParentRelation(ctx: RelationContext): Pair<String, String>? =
        ctx.m1Rel.parents
            .firstOrNull { it.second.memberId == ctx.m2Id }
            ?.let { (relM2ToM1, _) ->
                val relM1ToM2 =
                    if (ctx.isM1Male) RELATION_TYPE_SON else RELATION_TYPE_DAUGHTER
                relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
            }

    /**
     * Member1 is grandchild of Member2.
     * (i.e., Member1's grand parents (both) == Member2 )
     * example: Member 1 (Pratik) (Grand Son F/ pota) -> Member 2 (Narbat Lal Singh) (Grand father f/ dada ji)
     * example: Member 1 (Pratik) (Grand Son M/ naati) -> Member 2 (Sankar Lal) (Grand father M/ nana ji )
     * example: Member 1 (Payal) (Grand Daughter M/ naatin) -> Member 2 (Sankar Lal) (Grand father M/ nana ji )
     */
    private fun findGrandChildRelation(ctx: RelationContext): Pair<String, String>? =
        (ctx.m1Rel.grandParentsFather + ctx.m1Rel.grandParentsMother)
            .firstOrNull { it.second.memberId == ctx.m2Id }
            ?.let { (relM2ToM1, _) ->
                val relM1ToM2 = when (relM2ToM1) {
                    RELATION_TYPE_GRANDFATHER_F,
                    RELATION_TYPE_GRANDMOTHER_F ->
                        if (ctx.isM1Male) RELATION_TYPE_GRANDCHILD else RELATION_TYPE_GRANDCHILD_F

                    RELATION_TYPE_GRANDFATHER_M,
                    RELATION_TYPE_GRANDMOTHER_M ->
                        if (ctx.isM1Male) RELATION_TYPE_GRANDCHILD_MOTHER_SIDE else RELATION_TYPE_GRANDCHILD_MOTHER_SIDE_F

                    else ->
                        if (ctx.isM1Male) RELATION_TYPE_GRANDCHILD else RELATION_TYPE_GRANDCHILD_F
                }
                relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
            }

    /**
     * Member1 is grandparent of Member2.
     * example: Member 1 (Narbat Lal Singh) (Grand father f/ dada ji) -> Member 2 (Pratik) (Grand Son F/ pota)
     * example: Member 1 (Sankar Lal) (Grand father M/ nana ji ) -> Member 2 (Pratik) (Grand Son M/ naati)
     * example: Member 1 (Sankar Lal) (Grand father M/ nana ji ) -> Member 2 (Payal) (Grand Daughter M/ naatin)
     */
    private fun findGrandParentRelation(ctx: RelationContext): Pair<String, String>? =
        (ctx.m2Rel.grandParentsFather + ctx.m2Rel.grandParentsMother)
            .firstOrNull { it.second.memberId == ctx.m1Id }
            ?.let { (relM1ToM2, _) ->
                val relM2ToM1 =
                    if (ctx.isM2Male) RELATION_TYPE_GRANDCHILD else RELATION_TYPE_GRANDCHILD_F
                relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
            }

    /**
     * Resolves cousin relationships (father, mother, mixed).
     * example: Member 1 (Pratik) (chachera bhai) -> Member 2 (Satish Vishwakarma) (chachera bhai)
     * example: Member 1 (Pratik) (chachera bhai) -> Member 2 (Abhishek Vishwakarma) (Mamera bhai)
     * example: Member 1 (Abhishek Carpenter) (chachera bhai) -> Member 2 (Dharmendra) (Mamera bhai)
     */
    private fun findCousinRelation(ctx: RelationContext): Pair<String, String>? {
        fun commonGrandParent(
            a: List<Pair<String, FamilyMember>>,
            b: List<Pair<String, FamilyMember>>
        ) = a.any { g1 -> b.any { g2 -> g1.second.memberId == g2.second.memberId } }

        return when {
            // father side cousin
            commonGrandParent(ctx.m1Rel.grandParentsFather, ctx.m2Rel.grandParentsFather) -> {
                val rel1 = if (ctx.isM1Male)
                    RELATION_TYPE_COUSIN_BROTHER_FATHER_SIDE
                else RELATION_TYPE_COUSIN_SISTER_FATHER_SIDE

                val rel2 = if (ctx.isM2Male)
                    RELATION_TYPE_COUSIN_BROTHER_FATHER_SIDE
                else RELATION_TYPE_COUSIN_SISTER_FATHER_SIDE

                rel1.relationTextInHindi() to rel2.relationTextInHindi()
            }
            // mother side cousin
            commonGrandParent(ctx.m1Rel.grandParentsMother, ctx.m2Rel.grandParentsMother) -> {
                val rel1 = if (ctx.isM1Male)
                    RELATION_TYPE_COUSIN_BROTHER_MOTHER_SIDE
                else RELATION_TYPE_COUSIN_SISTER_MOTHER_SIDE

                val rel2 = if (ctx.isM2Male)
                    RELATION_TYPE_COUSIN_BROTHER_MOTHER_SIDE
                else RELATION_TYPE_COUSIN_SISTER_MOTHER_SIDE

                rel1.relationTextInHindi() to rel2.relationTextInHindi()
            }
            // father/Mother side cousin
            commonGrandParent(
                ctx.m1Rel.grandParentsFather + ctx.m1Rel.grandParentsMother,
                ctx.m2Rel.grandParentsFather + ctx.m2Rel.grandParentsMother
            ) -> {
                val rel1 =
                    if (ctx.isM1Male) RELATION_TYPE_COUSIN_BROTHER else RELATION_TYPE_COUSIN_SISTER
                val rel2 =
                    if (ctx.isM2Male) RELATION_TYPE_COUSIN_BROTHER else RELATION_TYPE_COUSIN_SISTER
                rel1.relationTextInHindi() to rel2.relationTextInHindi()
            }

            else -> null
        }
    }

    /* ---------- Uncle/Aunt ↔ Nephew/Niece (Father side) ----------
     * Condition:
     * Member2 is Uncle/Aunt of Member1's father
     * (i.e., Member1's  paternal grandparent == Member2's parent)
     * example: Member 1 (Pratik) (Bhatija) -> Member 2 (Ramesh Vishwakarma) (Tau Ji)
     * example: Member 1 (Satish) (Bhatija) -> Member 2 (Kailash Vishwakarma) (Chacha ji)
     * example: Member 1 (Pratik) (Bhatija) -> Member 2 (Kanta Carpenter) (Bua Ji)
     */
    private fun findFatherSideUncleAuntRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m1Rel.grandParentsFather.any { gp ->
            ctx.m2Rel.parents.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM1ToM2 =
            if (ctx.isM1Male) RELATION_TYPE_SON_OF_BROTHER
            else RELATION_TYPE_DAUGHTER_OF_BROTHER

        /* Find Member1's father */
        val father = ctx.m1Rel.parents
            .firstOrNull { it.first == RELATION_TYPE_FATHER }
            ?.second


        val relM2ToM1 = father?.let { m1Father ->
            val isElder = calculateAgeFromDob(m1Father.dob) < calculateAgeFromDob(ctx.m2.dob)
            when {
                ctx.isM2Male && isElder -> RELATION_TYPE_ELDER_BROTHER_OF_FATHER    // (Tau Ji)
                ctx.isM2Male -> RELATION_TYPE_YOUNGER_BROTHER_OF_FATHER // (Chacha ji)
                else -> RELATION_TYPE_SISTER_OF_FATHER  // (Bua Ji)
            }
        } ?: ""
        logger("getMembersBetweenRelations::findFatherSideUncleAuntRelation: Uncle/Aunt ↔ Nephew/Niece = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }

    /* ---------- Uncle/Aunt Spouse ↔ Nephew/Niece (Father side) ----------
    * Condition:
    * Member2 is Uncle/Aunt spouse of Member1's
    * (i.e., Member1's paternal grandparent == Member2's in laws)
    * example: Member 1 (Pratik) (Bhatija) -> Member 2 (Kanta Vishwakarma) (Taiji)
    * example: Member 1 (Satish) (Bhatija) -> Member 2 (Rajni Vishwakarma) (Chachi)
    * example: Member 1 (Pratik) (Bhatija) -> Member 2 (Radheshyam Carpenter) (Phupha Ji)
    */
    private fun findFatherSideUncleAuntSpouseRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m1Rel.grandParentsFather.any { gp ->
            ctx.m2Rel.inLaws.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM1ToM2 =
            if (ctx.isM1Male) RELATION_TYPE_SON_OF_BROTHER
            else RELATION_TYPE_DAUGHTER_OF_BROTHER

        /* Find Member1's father */
        val father = ctx.m1Rel.parents
            .firstOrNull { it.first == RELATION_TYPE_FATHER }
            ?.second


        val relM2ToM1 = father?.let { m1Father ->
            val isElder = calculateAgeFromDob(m1Father.dob) < calculateAgeFromDob(
                ctx.m2Rel.spouse?.second?.dob ?: ""
            )
            when {
                !ctx.isM2Male && isElder -> RELATION_TYPE_WIFE_OF_ELDER_BROTHER_OF_FATHER // (Tai Ji)
                !ctx.isM2Male -> RELATION_TYPE_WIFE_OF_YOUNGER_BROTHER_OF_FATHER // (Chachi Ji)
                else -> RELATION_TYPE_HUSBAND_OF_SISTER_OF_FATHER // (Phupha Ji)
            }
        } ?: ""

        logger("getMembersBetweenRelations::findFatherSideUncleAuntSpouseRelation: Uncle/Aunt ↔ Nephew/Niece = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }


    /* ---------- Nephew/Niece ↔ Uncle/Aunt (Father side) ----------
     * Condition:
     * Member1 is Nephew/Niece of Member2's father
     * (i.e., Member1's parent == Member2's paternal grandparent)
     * example: Member 1 (Pratik) (Bhatija) -> Member 2 (Ramesh Vishwakarma) (Tauji)
     * example: Member 1 (Satish) (Bhatija) -> Member 2 (Kailash Vishwakarma) (chacha ji)
     * example: Member 1 (Pratik) (Bhatija) -> Member 2 (Kanta Carpenter) (Bua Ji)
     */
    private fun findFatherSideNephewNieceRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m2Rel.grandParentsFather.any { gp ->
            ctx.m1Rel.parents.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM2ToM1 =
            if (ctx.isM1Male) RELATION_TYPE_SON_OF_BROTHER
            else RELATION_TYPE_DAUGHTER_OF_BROTHER

        /* Find Member1's father */
        val father = ctx.m1Rel.parents
            .firstOrNull { it.first == RELATION_TYPE_FATHER }
            ?.second

        val relM1ToM2 = father?.let { m1Father ->
            val isElder = calculateAgeFromDob(m1Father.dob) < calculateAgeFromDob(ctx.m2.dob)
            when {
                ctx.isM1Male && isElder -> RELATION_TYPE_ELDER_BROTHER_OF_FATHER    // (Tau Ji)
                ctx.isM1Male -> RELATION_TYPE_YOUNGER_BROTHER_OF_FATHER // (Chacha ji)
                else -> RELATION_TYPE_SISTER_OF_FATHER  // (Bua Ji)
            }
        } ?: ""
        logger("getMembersBetweenRelations::findFatherSideNephewNieceRelation: nephew/niece = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }

    /* ----------  Nephew/Niece ↔ Uncle/Aunt Spouse (Father side) ----------
    * Condition:
    * Member1 is Nephew/Niece spouse of Member2's father
    * (i.e., Member1's in laws == Member2's paternal grandparent)
    * example: Member 1 (Kanta Vishwakarma) (Taiji) -> Member 2 (Pratik) (Bhatija)
    * example: Member 1 (Rajni Vishwakarma) (Chachi) -> Member 2 (Satish) (Bhatija)
    * example: Member 1 (Radheshyam Carpenter) (Phupha Ji) -> Member 2 (Pratik) (Bhatija)
    */
    private fun findFatherSideNephewNieceSpouseRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m2Rel.grandParentsFather.any { gp ->
            ctx.m1Rel.inLaws.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM2ToM1 =
            if (ctx.isM1Male) RELATION_TYPE_SON_OF_BROTHER
            else RELATION_TYPE_DAUGHTER_OF_BROTHER

        /* Find Member1's father */
        val father = ctx.m2Rel.parents
            .firstOrNull { it.first == RELATION_TYPE_FATHER }
            ?.second


        val relM1ToM2 = father?.let { m2Father ->
            val isElder = calculateAgeFromDob(m2Father.dob) < calculateAgeFromDob(
                ctx.m1Rel.spouse?.second?.dob ?: ""
            )
            when {
                !ctx.isM1Male && isElder -> RELATION_TYPE_WIFE_OF_ELDER_BROTHER_OF_FATHER // (Taiji)
                !ctx.isM1Male -> RELATION_TYPE_WIFE_OF_YOUNGER_BROTHER_OF_FATHER // (Chachi)
                else -> RELATION_TYPE_HUSBAND_OF_SISTER_OF_FATHER // (Phupha Ji)
            }
        } ?: ""

        logger("getMembersBetweenRelations::findFatherSideNephewNieceSpouseRelation: Uncle/Aunt ↔ Nephew/Niece = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }

    /* ---------- Uncle/Aunt ↔ Nephew/Niece (Mother side) ----------
     * Condition:
     * Member2 is Uncle/Aunt of Member1's (Mother side)
     * (i.e., Member1's maternal grandparent == Member2's parent)
     * example: Member 1 (Pratik Vishwakarma) (Bhanja) -> Member 2 (Jitendra Vishwakarma) (Mama)
     * example: Member 1 (Payal Vishwakarma) (Bhanji) -> Member 2 (Jitendra Vishwakarma) (Mama)
     * example: Member 1 (Abhishek Carpenter) (Bhanja) -> Member 2 (Lakshmi Piplodiya) (Mashi)
     */
    private fun findMotherSideUncleAuntRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m1Rel.grandParentsMother.any { gp ->
            ctx.m2Rel.parents.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM1ToM2 =
            if (ctx.isM1Male) RELATION_TYPE_SON_OF_SISTER   // (bhanja)
            else RELATION_TYPE_DAUGHTER_OF_SISTER   // (bhanji)

        val relM2ToM1 =
            if (ctx.isM2Male) RELATION_TYPE_BROTHER_OF_MOTHER   // (mama)
            else RELATION_TYPE_SISTER_OF_MOTHER // (Mashi)
        logger("getMembersBetweenRelations::findMotherSideUncleAuntRelation: Uncle/Aunt ↔ Nephew/Niece (Mother Side) = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }

    /* ---------- Uncle/Aunt Spouse ↔ Nephew/Niece (Mother side) ----------
    * Condition:
    * Member2 is Uncle/Aunt's spouse of Member1
    * (i.e., Member1's paternal grandparent == Member2's in laws)
    * example: Member 1 (Pratik Vishwakarma) (Bhanja) -> Member 2 (Neetu Vishwakarma) (Mami)
    * example: Member 1 (Payal Vishwakarma) (Bhanji) -> Member 2 (Rekha Vishwakarma) (Mami)
    * example: Member 1 (Abhishek Carpenter) (Bhanja) -> Member 2 (Chagan Lal Piplodiya) (Mosa ji)
    */
    private fun findMotherSideUncleAuntSpouseRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m1Rel.grandParentsMother.any { gp ->
            ctx.m2Rel.inLaws.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM1ToM2 =
            if (ctx.isM1Male) RELATION_TYPE_SON_OF_SISTER
            else RELATION_TYPE_DAUGHTER_OF_SISTER

        val relM2ToM1 =
            if (ctx.isM2Male) RELATION_TYPE_HUSBAND_OF_SISTER_OF_MOTHER   // (Mosa ji)
            else RELATION_TYPE_WIFE_OF_BROTHER_OF_MOTHER // (Mami ji)

        logger("getMembersBetweenRelations::findMotherSideUncleAuntSpouseRelation: Uncle/Aunt ↔ Nephew/Niece = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }

    /* ---------- Nephew/Niece ↔ Uncle/Aunt (Mother side) ----------
     * Condition:
     * Member2 is Nephew/Niece of Member1's Mother side
     * (i.e., Member1's parent == Member2's maternal grandparent)
     * example: Member 1 (Jitendra Vishwakarma) (Mama) -> Member 2 (Pratik Vishwakarma) (Bhanja)
     * example: Member 1 (Jitendra Vishwakarma) (Mama) -> Member 2 (Payal Vishwakarma) (Bhanji)
     * example: Member 1 (Lakshmi Piplodiya) (Mashi) ->  Member 2 (Abhishek Carpenter) (Bhanja)
     */
    private fun findMotherSideNephewNieceRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m2Rel.grandParentsMother.any { gp ->
            ctx.m1Rel.parents.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM1ToM2 =
            if (ctx.isM1Male) RELATION_TYPE_BROTHER_OF_MOTHER   // (mama)
            else RELATION_TYPE_SISTER_OF_MOTHER // (Mashi)

        val relM2ToM1 =
            if (ctx.isM2Male) RELATION_TYPE_SON_OF_SISTER   // (bhanja)
            else RELATION_TYPE_DAUGHTER_OF_SISTER   // (bhanji)
        logger("getMembersBetweenRelations::findMotherSideUncleAuntRelation: Uncle/Aunt ↔ Nephew/Niece (Mother Side) = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }


    /* ---------- Nephew/Niece ↔ Uncle/Aunt Spouse (Mother side) ----------
    * Condition:
    * Member1 is Uncle/Aunt's spouse of Member1's  Mother side
    * (i.e., Member1's in laws == Member2's paternal grandparent)
    * example: Member 1 (Neetu Vishwakarma) (Mami) -> Member 2 (Pratik Vishwakarma) (Bhanja)
    * example: Member 1 (Rekha Vishwakarma) (Mami) -> Member 2 (Payal Vishwakarma) (Bhanji)
    * example: Member 1 (Chagan Lal Piplodiya) (Mosa ji) -> Member 2 (Abhishek Carpenter) (Bhanja)
    */
    private fun findMotherSideNephewNieceSpouseRelation(ctx: RelationContext): Pair<String, String>? {
        val isRelation = ctx.m2Rel.grandParentsMother.any { gp ->
            ctx.m1Rel.inLaws.any { it.second.memberId == gp.second.memberId }
        }
        if (!isRelation) return null

        val relM1ToM2 = if (ctx.isM1Male) RELATION_TYPE_HUSBAND_OF_SISTER_OF_MOTHER // (Mosa ji)
        else RELATION_TYPE_WIFE_OF_BROTHER_OF_MOTHER   // (mami)

        val relM2ToM1 =
            if (ctx.isM2Male) RELATION_TYPE_SON_OF_SISTER
            else RELATION_TYPE_DAUGHTER_OF_SISTER

        logger("getMembersBetweenRelations::findMotherSideNephewNieceSpouseRelation: Uncle/Aunt ↔ Nephew/Niece = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }


    /* ---------- Jija ↔ Sala/Sali or Bhabhi ↔ Devar/Nanand ----------
    * Condition:
    * Member1 is Jija Ji of Member2
    * (i.e., Member1's spouse == Member2's sister)
    * example: Member 1 (Chaggan Lal Piplodiya) (Jija ji) -> Member 2 (Kailash Vishwakarma) (Sala)
    * example: Member 1 (Kanta Vishwakarma) (Bhabhi ji) -> Member 2 (Kailash Vishwakarma) (Devar)
    * example: Member 1 (Kanta Vishwakarma) (Bhabhi ji) -> Member 2 (Kanta Carpenter) (Nanand)
    * example: Member 1 (Chaggan Lal Piplodiya) (Jija ji) -> Member 2 (Kanta Carpenter) (Sali)
    */
    private fun findJijaSalaBhabhiDevarOrNanandRelation(ctx: RelationContext): Pair<String, String>? {
        val sibling =
            ctx.m2Rel.siblings.firstOrNull { sibling -> sibling.second.memberId == ctx.m1Rel.spouse?.second?.memberId }

        if (sibling == null) return null

        val relM1ToM2 =
            if (ctx.isM1Male) RELATION_TYPE_HUSBAND_OF_SISTER /*Jija ji*/ else RELATION_TYPE_WIFE_OF_BROTHER /*Bhabhi ji*/
        val relM2ToM1 = when {
            relM1ToM2 == RELATION_TYPE_WIFE_OF_BROTHER && ctx.isM2Male -> RELATION_TYPE_BROTHER_OF_HUSBAND  /*devar*/
            relM1ToM2 == RELATION_TYPE_WIFE_OF_BROTHER && !ctx.isM2Male -> RELATION_TYPE_SISTER_OF_HUSBAND  /*nanad*/
            ctx.isM2Male -> RELATION_TYPE_BROTHER_OF_WIFI  /*sala*/
            else -> RELATION_TYPE_SISTER_OF_WIFI  /*sali*/
        }

        logger("getMembersBetweenRelations::findJijaSalaBhabhiDevarOrNanandRelation: = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }


    /* ---------- Sala/Sali ↔ Jija or Devar/Nanand ↔ Bhabhi ----------
    * Condition:
    * Member1 is Jija Ji of Member2
    * (i.e., Member1's spouse == Member2's sister)
    * example: Member 1 (Kailash Vishwakarma) (Sala) -> Member 2 (Chaggan Lal Piplodiya) (Jija ji)
    * example: Member 1 (Kailash Vishwakarma) (Devar) -> Member 2 (Kanta Vishwakarma) (Bhabhi ji)
    * example: Member 1 (Kanta Carpenter) (Nanand) -> Member 2 (Kanta Vishwakarma) (Bhabhi ji)
    * example: Member 1 (Kanta Carpenter) (Sali) -> Member 2 (Chaggan Lal Piplodiya) (Jija ji)
    */
    private fun findSalaJijaDevarOrNanandBhabhiRelation(ctx: RelationContext): Pair<String, String>? {
        val sibling =
            ctx.m1Rel.siblings.firstOrNull { sibling -> sibling.second.memberId == ctx.m2Rel.spouse?.second?.memberId }

        if (sibling == null) return null

        val relM2ToM1 =
            if (ctx.isM2Male) RELATION_TYPE_HUSBAND_OF_SISTER /*Jija ji*/ else RELATION_TYPE_WIFE_OF_BROTHER /*Bhabhi ji*/
        val relM1ToM2 = when {
            relM2ToM1 == RELATION_TYPE_WIFE_OF_BROTHER && ctx.isM1Male -> RELATION_TYPE_BROTHER_OF_HUSBAND  /*devar*/
            relM2ToM1 == RELATION_TYPE_WIFE_OF_BROTHER && !ctx.isM1Male -> RELATION_TYPE_SISTER_OF_HUSBAND  /*nanad*/
            ctx.isM1Male -> RELATION_TYPE_BROTHER_OF_WIFI  /*sala*/
            else -> RELATION_TYPE_SISTER_OF_WIFI  /*sali*/
        }

        logger("getMembersBetweenRelations::findJijaSalaBhabhiDevarOrNanandRelation: = $relM1ToM2, $relM2ToM1")
        return relM1ToM2.relationTextInHindi() to relM2ToM1.relationTextInHindi()
    }


    /**
     * helper function to label the actual relations of the member
     * */
    private suspend fun getRelations(
        memberId: Int,
        relations: List<FamilyRelation>,
        relatives: MemberRelationAR
    ) {
        logger("getRelations called for memberId: $memberId")
        relations.forEach { relation ->
            val memberDetails = getMemberById(relation.relatedMemberId)
            memberDetails?.let {
                when (relation.relationType) {
                    RELATION_TYPE_FATHER -> {
                        relatives.parents.add(Pair(RELATION_TYPE_FATHER, it))
                        fetchGrandParents(it, isParental = true, relatives = relatives)
                    }

                    RELATION_TYPE_MOTHER -> {
                        relatives.parents.add(Pair(RELATION_TYPE_MOTHER, it))
                        fetchGrandParents(it, isParental = false, relatives = relatives)
                    }

                    RELATION_TYPE_WIFE -> {
                        relatives.spouse = Pair(RELATION_TYPE_WIFE, it)
                    }

                    RELATION_TYPE_HUSBAND -> {
                        relatives.spouse = Pair(RELATION_TYPE_HUSBAND, it)
                    }
                }
            }
        }
        relatives.spouse?.let {
            fetchInLawsDetails(it.second, relatives)
            fetchChildren(memberId, relatives)
        }
        fetchSiblings(memberId, relatives)
    }


    private suspend fun fetchGrandParents(
        member: FamilyMember,
        isParental: Boolean = true,
        relatives: MemberRelationAR
    ) {
        logger("fetchGrandParents for isParental: $isParental  member: $member")
        val parentsWithMemberId = getParentsWithMemberId(member.memberId)
        logger("fetchGrandParents: $parentsWithMemberId")
        parentsWithMemberId.forEach { parent ->
            when (parent.first) {
                RELATION_TYPE_FATHER -> {
                    if (isParental)
                        relatives.grandParentsFather.add(
                            Pair(
                                RELATION_TYPE_GRANDFATHER_F,
                                parent.second
                            )
                        )
                    else
                        relatives.grandParentsMother.add(
                            Pair(
                                RELATION_TYPE_GRANDFATHER_M,
                                parent.second
                            )
                        )
                }

                RELATION_TYPE_MOTHER -> {
                    if (isParental)
                        relatives.grandParentsFather.add(
                            Pair(
                                RELATION_TYPE_GRANDMOTHER_F,
                                parent.second
                            )
                        )
                    else
                        relatives.grandParentsMother.add(
                            Pair(
                                RELATION_TYPE_GRANDMOTHER_M,
                                parent.second
                            )
                        )
                }
            }
        }
    }


    /**
     * this will fetch the member's In-Laws details
     * */
    private suspend fun fetchInLawsDetails(
        spouse: FamilyMember,
        relatives: MemberRelationAR
    ) {
        logger("fetchInLawsDetails for spouse: $spouse")
        val parentsWithMemberId = getParentsWithMemberId(spouse.memberId)
        logger("parentsWithMemberId: $parentsWithMemberId")
        parentsWithMemberId.forEach { parent ->
            when (parent.first) {
                RELATION_TYPE_FATHER -> {
                    relatives.inLaws.add(Pair(RELATION_TYPE_FATHER_IN_LAW, parent.second))
                }

                RELATION_TYPE_MOTHER -> {
                    relatives.inLaws.add(Pair(RELATION_TYPE_MOTHER_IN_LAW, parent.second))
                }
            }
        }
    }


    private suspend fun fetchChildren(memberId: Int, relatives: MemberRelationAR) {
        logger("fetchChildren: for member: $memberId")
        val children = getChildrenWithSpouse(memberId)
        logger("total children: $children")
        children.forEach { child ->
            if (child.child.memberId != memberId) {
                logger("child: $child")
                when (child.child.gender) {
                    GENDER_TYPE_MALE -> {
                        relatives.children.add(Pair(RELATION_TYPE_SON, child))
                    }

                    GENDER_TYPE_FEMALE -> {
                        relatives.children.add(Pair(RELATION_TYPE_DAUGHTER, child))
                    }
                }
            }
        }
    }


    private suspend fun fetchSiblings(memberId: Int, relatives: MemberRelationAR) {
        relatives.parents.forEach {
            logger("FetchSiblings: for member: $it")
            val siblings = getChildren(it.second.memberId)
            logger("siblings: $siblings")
            siblings.forEach { sibling ->
                if (sibling.memberId != memberId) {
                    logger("sibling: $sibling")
                    when (sibling.gender) {
                        GENDER_TYPE_MALE ->
                            relatives.siblings.add(Pair(RELATION_TYPE_BROTHER, sibling))

                        GENDER_TYPE_FEMALE ->
                            relatives.siblings.add(Pair(RELATION_TYPE_SISTER, sibling))
                    }
                }
            }
        }
    }


    override suspend fun getMemberSmallBio(memberRelatives: MemberRelationAR): String {
        logger("getMemberSmallBio: ${memberRelatives.member?.fullName}")
        val member = memberRelatives.member ?: return ""

        val memberName = member.fullName

        val fatherName =
            memberRelatives.parents
                .firstOrNull { it.first == RELATION_TYPE_FATHER }
                ?.second?.fullName

        val motherName =
            memberRelatives.parents
                .firstOrNull { it.first == RELATION_TYPE_MOTHER }
                ?.second?.fullName

        val spouseName = memberRelatives.spouse?.second?.fullName

        val isMale = member.gender == GENDER_TYPE_MALE
        val childWord = if (isMale) HiText.MALE_CHILD else HiText.FEMALE_CHILD

        val bio = StringBuilder()

        // --- Birth / Death ---
        bio.append("$memberName ${HiText.BORN_} ${member.dob} ${HiText.ON}")
        if (!member.isLiving) {
            bio.append(" ${HiText.DIED} ${member.dod} ${HiText.ON} हुआ")
        }
        bio.append("। ")

        // --- Parents ---
        bio.append(
            "${HiText.VE} ${HiText.SHREE} $fatherName ${HiText.AND} " +
                    "${HiText.SHREEMATI} $motherName के $childWord ${HiText.IS}।\n"
        )

        // --- Marriage ---
        if (!spouseName.isNullOrBlank()) {
            bio.append(
                "${HiText.HAS_SINGLE} ${HiText.MARRIAGE} ${HiText.SHREEMATI} $spouseName से हुआ।\n\n"
            )
        }

        // --- Children ---
        if (memberRelatives.children.isNotEmpty()) {
            if (memberRelatives.children.size == 1) {
                val firstChild = memberRelatives.children[0]
                val childGender = if (firstChild.first == RELATION_TYPE_SON)
                    HiText.MALE_CHILD
                else
                    HiText.FEMALE_CHILD
                bio.append(
                    "${HiText.HAS} ${HiText.ONLY_ONE} $childGender, ${firstChild.second.child.fullName} हैं।\n"
                )
            } else {
                bio.append(
                    "${HiText.HAS} ${memberRelatives.children.size} ${HiText.CHILDREN} हैं।\n"
                )

                memberRelatives.children.forEachIndexed { index, child ->
                    val childName = child.second.child.fullName
                    val genderText =
                        if (child.first == RELATION_TYPE_SON)
                            HiText.MALE_CHILD
                        else
                            HiText.FEMALE_CHILD

                    bio.append("$childName $genderText")

                    when (index) {
                        memberRelatives.children.lastIndex - 1 -> bio.append(", और ")
                        memberRelatives.children.lastIndex -> bio.append("")
                        else -> bio.append(", ")
                    }
                }
            }
        }
        return bio.toString().trim()
    }


    /**
     * to create the timeline for the first member
     * */
    override suspend fun createMemberTimeline(memberRelatives: MemberRelationAR): List<TimelineEvent> {
        logger("createMemberTimeline: ${memberRelatives.member?.fullName}")
        if (memberRelatives.member == null) return emptyList()
        val member = memberRelatives.member!!
        val timeline = mutableListOf<TimelineEvent>()
        memberRelatives.spouse?.let { spouse ->
            timeline.add(
                TimelineEvent(
                    TimelineEventType.MARRIAGE,
                    "${spouse.second.fullName} ${HiText.MARRIED_TO}",
                    "",
                    ""
                )
            )
            if (!spouse.second.isLiving) {
                val relation =
                    if (spouse.second.gender == GENDER_TYPE_MALE) RELATION_TYPE_HUSBAND else RELATION_TYPE_WIFE
                timeline.add(
                    TimelineEvent(
                        TimelineEventType.DEATH,
                        "${relation.inHindi()} ${HiText.DIED_ON}",
                        "",
                        spouse.second.dod
                    )
                )
            }
        }
        if (memberRelatives.children.isNotEmpty()) {
            memberRelatives.children.forEach {
                if (it.second.child.gender == GENDER_TYPE_MALE)
                    timeline.add(
                        TimelineEvent(
                            TimelineEventType.SON_BIRTH,
                            "${HiText.SON_BORN}: ${it.second.child.fullName}",
                            "",
                            it.second.child.dob
                        )
                    )
                else
                    timeline.add(
                        TimelineEvent(
                            TimelineEventType.DAUGHTER_BIRTH,
                            "${HiText.DAUGHTER_BORN}: ${it.second.child.fullName}",
                            "",
                            it.second.child.dob
                        )
                    )
            }
        }
        if (!member.isLiving) {
            timeline.add(
                TimelineEvent(
                    TimelineEventType.DEATH,
                    HiText.DIEDED,
                    "",
                    member.dod
                )
            )
        }
        timeline.sortBy { it.date }
        timeline.add(0,
            TimelineEvent(
                TimelineEventType.BIRTH,
                HiText.BORN,
                "${member.city}, ${member.state}",
                member.dob,
            ),
        )
        return timeline
    }


}

