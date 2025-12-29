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
import com.pratik.learning.familyTree.utils.DATA_BACKUP_FILE_NAME
import com.pratik.learning.familyTree.utils.DATA_BACKUP_FILE_PATH
import com.pratik.learning.familyTree.utils.GENDER_TYPE_FEMALE
import com.pratik.learning.familyTree.utils.GENDER_TYPE_MALE
import com.pratik.learning.familyTree.utils.RELATION_TYPE_BROTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_DAUGHTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_FATHER_IN_LAW
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDCHILD_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDFATHER_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDFATHER_M
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDMOTHER_F
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GRANDMOTHER_M
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GREAT_GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GREAT_GREAT_GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_GREAT____GRANDCHILD
import com.pratik.learning.familyTree.utils.RELATION_TYPE_HUSBAND
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_MOTHER_IN_LAW
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SISTER
import com.pratik.learning.familyTree.utils.RELATION_TYPE_SON
import com.pratik.learning.familyTree.utils.RELATION_TYPE_WIFE
import com.pratik.learning.familyTree.utils.SyncPrefs.setIsDataUpdateRequired
import com.pratik.learning.familyTree.utils.SyncPrefs.setLastSyncTime
import com.pratik.learning.familyTree.utils.inHindi
import com.pratik.learning.familyTree.utils.logger
import com.pratik.learning.familyTree.utils.relationTextInHindi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.collections.distinct
import kotlin.collections.plus

class FamilyTreeRepositoryImpl(
    private val dao: FamilyTreeDao,
    private val storage: FirebaseStorage,
    @ApplicationContext private val context: Context
): FamilyTreeRepository  {
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

    override suspend fun updateMember(member: FamilyMember, isGotraChanged: Boolean, spouseId: Int) {
        logger("updateMember: $member  isGotraChanged = $isGotraChanged  spouseId = $spouseId")
        dao.updateGotra(member.memberId, member.gotra)
        if (spouseId != -1 ) {
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

    override fun getPagedMembersForSearchByName(name: String, isUnmarried: Boolean): Flow<PagingData<MemberWithFather>> {
        logger("getPagedMembersForSearchByName Loading page with matched: $name")
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { dao.getAllMembersBySearchQuery(name, isUnmarried) }
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
        level:Int,
        familyTreeDao: FamilyTreeDao,
        isParental: Boolean = true
    ): AncestorNode {
        logger("buildAncestryNode:: called for member = ${member.fullName},  isParental = $isParental")
        // 1. Get Spouse using getSpouse()
        val spouse = familyTreeDao.getSpouse(member.memberId)

        // 2. Recursively find Father's line
        val father = familyTreeDao.getImmediateParentByType(member.memberId, "Father")
        val fatherNode = father?.let { buildAncestryNode(it, level = level + 1, familyTreeDao, isParental) }

        // 3. Recursively find Mother's line
        val mother = familyTreeDao.getImmediateParentByType(member.memberId, "Mother")
        val motherNode = mother?.let { buildAncestryNode(it, level = level + 1, familyTreeDao, isParental) }
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
            relationWithMember = getRelationWithMember(level+1, isParental)
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
            relationWithMember = getDescendantRelationWithMember(level, member.gender == GENDER_TYPE_MALE)
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
        return dao.getChildren(memberId)?: emptyList()
    }

    override suspend fun getChildrenWithSpouse(memberId: Int): List<ChildWithSpouseDto> {
        logger("getChildrenWithSpouse:: memberId = $memberId")
        return dao.getChildrenWithSpouse(memberId)
    }

    override suspend fun downloadDataFromServer() : Boolean {
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
        return relatives
    }

    override suspend fun getMembersBetweenRelations(
        member1Relatives: MemberRelationAR,
        member2Relatives: MemberRelationAR
    ): Pair<String, String> {
        logger("getMembersBetweenRelations:: member1Relatives = ${member1Relatives.member}, member2Relatives = ${member2Relatives.member}")
        var member1RelationToMember2 = ""
        var member2RelationToMember1 = ""
        if (member1Relatives.member == null || member2Relatives.member == null)
            return Pair(member1RelationToMember2, member2RelationToMember1)
        // Sibling relations
        val siblings =
            member1Relatives.siblings.filter{ it.second.memberId == member2Relatives.member?.memberId }
        if (siblings.isNotEmpty()) {
            member1RelationToMember2 = if (member1Relatives.member?.gender == GENDER_TYPE_MALE) RELATION_TYPE_BROTHER else RELATION_TYPE_SISTER
            member2RelationToMember1 = siblings[0].first
            return Pair(member1RelationToMember2.relationTextInHindi(), member2RelationToMember1.relationTextInHindi())
        }

        // Parent - child relation
        val children =
            member1Relatives.children.filter{ it.second.child.memberId == member2Relatives.member?.memberId }
        if (children.isNotEmpty()) {
            member1RelationToMember2 = if (member1Relatives.member?.gender == GENDER_TYPE_MALE) RELATION_TYPE_FATHER else RELATION_TYPE_MOTHER
            member2RelationToMember1 = children[0].first
            return Pair(member1RelationToMember2.relationTextInHindi(), member2RelationToMember1.relationTextInHindi())
        }

        // Child - Parent relation
        val parent = member1Relatives.parents.filter { it.second.memberId == member2Relatives.member?.memberId }
        if (parent.isNotEmpty()) {
            member1RelationToMember2 = if (member1Relatives.member?.gender == GENDER_TYPE_MALE) RELATION_TYPE_SON else RELATION_TYPE_DAUGHTER
            member2RelationToMember1 = parent[0].first
            return Pair(member1RelationToMember2.relationTextInHindi(), member2RelationToMember1.relationTextInHindi())
        }

        // Grand Child - Parent relation
        val grandParents = (member1Relatives.grandParentsFather + member1Relatives.grandParentsMother).filter { it.second.memberId == member2Relatives.member?.memberId  }
        if (grandParents.isNotEmpty()) {
            member1RelationToMember2 = if (member1Relatives.member?.gender == GENDER_TYPE_MALE) RELATION_TYPE_GRANDCHILD else RELATION_TYPE_GRANDCHILD_F
            member2RelationToMember1 = grandParents[0].first
            logger("getMembersBetweenRelations:: grandParents = $grandParents")
            return Pair(member1RelationToMember2.relationTextInHindi(), member2RelationToMember1.relationTextInHindi())
        }

        // member1 is grand parent of member2
        val grandChildren =  (member2Relatives.grandParentsFather + member2Relatives.grandParentsMother).filter { it.second.memberId == member1Relatives.member?.memberId  }
        if (grandChildren.isNotEmpty()) {
            member2RelationToMember1 = if (member2Relatives.member?.gender == GENDER_TYPE_MALE) RELATION_TYPE_GRANDCHILD else RELATION_TYPE_GRANDCHILD_F
            member1RelationToMember2 = grandChildren[0].first
            logger("getMembersBetweenRelations:: grandChildren = $grandChildren")
            return Pair(member1RelationToMember2.relationTextInHindi(), member2RelationToMember1.relationTextInHindi())
        }


        return Pair(member1RelationToMember2, member2RelationToMember1)


    }


    /**
     * helper function to label the actual relations of the member
     * */
    private suspend fun getRelations(memberId: Int, relations: List<FamilyRelation>, relatives: MemberRelationAR) {
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
                        relatives.grandParentsFather.add(Pair(RELATION_TYPE_GRANDFATHER_F, parent.second))
                    else
                        relatives.grandParentsMother.add(Pair(RELATION_TYPE_GRANDFATHER_M, parent.second))
                }
                RELATION_TYPE_MOTHER -> {
                    if (isParental)
                        relatives.grandParentsFather.add(Pair(RELATION_TYPE_GRANDMOTHER_F, parent.second))
                    else
                        relatives.grandParentsMother.add(Pair(RELATION_TYPE_GRANDMOTHER_M, parent.second))
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


}