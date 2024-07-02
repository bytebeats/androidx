/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.health.connect.client.testing

import androidx.health.connect.client.ExperimentalHealthConnectApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.aggregate.AggregationResultGroupedByDuration
import androidx.health.connect.client.aggregate.AggregationResultGroupedByPeriod
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.changes.DeletionChange
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.impl.converters.datatype.RECORDS_TYPE_NAME_MAP
import androidx.health.connect.client.impl.converters.records.toProto
import androidx.health.connect.client.impl.converters.records.toRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ChangesResponse
import androidx.health.connect.client.response.InsertRecordsResponse
import androidx.health.connect.client.response.ReadRecordResponse
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Clock
import kotlin.reflect.KClass

/**
 * Fake [HealthConnectClient] to be used in tests for components that use it as a dependency.
 *
 * Features:
 * * Add, remove, delete and read records using an in-memory object, supporting pagination.
 * * Token generation and change tracking.
 *
 * Note that this fake does not check for permissions.
 *
 * @param packageName the name of the package to use to generate unique record IDs.
 * @param clock used to close open-ended [TimeRangeFilter]s and record update times.
 * @param permissionController grants and revokes permissions.
 */
@ExperimentalHealthConnectApi
public class FakeHealthConnectClient(
    private val packageName: String = DEFAULT_PACKAGE_NAME,
    private val clock: Clock = Clock.systemDefaultZone(),
    override val permissionController: PermissionController = FakePermissionController()
) : HealthConnectClient {

    private val idsToRecords: MutableMap<String, Record> = mutableMapOf()
    private val deletedIdsToRecords: MutableMap<String, Record> = mutableMapOf()

    // Changes are tracked with a map of changes. The key is incremented with each change.
    private val tokens = mutableMapOf<String, TokenInfo>()
    private val timeToChanges: MutableMap<Long, Change> = mutableMapOf()
    private var timeToChangesLastKey = 0L

    private var idCounter = 0

    /**
     * Overrides the page size to test pagination when calling [getChanges].
     *
     * This is typically used with a low number (such as 2) so that a low number of inserted records
     * (such as 3) generate multiple pages. Use it to test token expiration as well.
     */
    public var pageSizeGetChanges: Int = 1000

    /**
     * Fake implementation that inserts one or more [Record]s into the in-memory store.
     *
     * Supports deduplication of
     * [androidx.health.connect.client.records.metadata.Metadata.clientRecordId]s using
     * [androidx.health.connect.client.records.metadata.Metadata.clientRecordVersion] to determine
     * precedence.
     */
    override suspend fun insertRecords(records: List<Record>): InsertRecordsResponse {
        val recordIdsList = mutableListOf<String>()
        records.forEach { record ->
            val recordId =
                record.metadata.clientRecordId?.toRecordId(packageName) ?: "testHCid${idCounter++}"
            recordIdsList += recordId
            val insertedRecord =
                toRecord(
                    record
                        .toProto()
                        .toBuilder()
                        .setUid(recordId)
                        .setUpdateTimeMillis(clock.millis())
                        .build()
                )
            // If the recordId exists and the existing clientRecordVersion is higher, don't insert.
            val newClientRecordVersion = insertedRecord.metadata.clientRecordVersion
            val existingClientRecordVersion =
                idsToRecords[recordId]?.metadata?.clientRecordVersion ?: -1
            if (newClientRecordVersion >= existingClientRecordVersion) {
                idsToRecords[recordId] = insertedRecord
                addUpsertionChange(insertedRecord)
            }
        }
        return InsertRecordsResponse(recordIdsList)
    }

    override suspend fun updateRecords(records: List<Record>) {
        // Check if all records belong to the package
        if (records.any { it.packageName != packageName }) {
            throw SecurityException("Trying to delete records owned by another package")
        }

        // Fake implementation
        records.forEach { record ->
            val recordId =
                record.metadata.clientRecordId?.toRecordId(packageName) ?: record.metadata.id

            val updatedRecord =
                toRecord(record.toProto().toBuilder().setUpdateTimeMillis(clock.millis()).build())
            idsToRecords[recordId] = updatedRecord
            removeUpsertion(recordId)
            addUpsertionChange(updatedRecord)
        }
    }

    override suspend fun deleteRecords(
        recordType: KClass<out Record>,
        recordIdsList: List<String>,
        clientRecordIdsList: List<String>
    ) {
        // Check if all records belong to the package in recordsIdsList
        if (
            recordIdsList
                .asSequence()
                .mapNotNull { idsToRecords[it]?.packageName }
                .any { it != packageName }
        ) {
            throw SecurityException("Trying to delete records owned by another package")
        }

        // Check if all records belong to the package in clientRecordIdsList
        if (
            clientRecordIdsList
                .asSequence()
                .mapNotNull { idsToRecords[it.toRecordId(packageName)]?.packageName }
                .any { it != packageName }
        ) {
            throw SecurityException("Trying to delete records owned by another package")
        }

        // Fake implementation
        recordIdsList.forEach { recordId ->
            idsToRecords[recordId]?.let { deletedIdsToRecords[recordId] = it }
            idsToRecords.remove(recordId)
            removeUpsertion(recordId)
            addDeletionChange(recordId)
        }
        clientRecordIdsList.forEach {
            val recordId = it.toRecordId(packageName)
            idsToRecords[recordId]?.let { deletedIdsToRecords[recordId] = it }
            idsToRecords.remove(recordId)
            addDeletionChange(recordId)
        }
    }

    override suspend fun deleteRecords(
        recordType: KClass<out Record>,
        timeRangeFilter: TimeRangeFilter
    ) {
        val recordIdsToRemove =
            idsToRecords
                .filterValues { record ->
                    record::class == recordType && record.isWithin(timeRangeFilter, clock)
                }
                .keys
        for (recordId in recordIdsToRemove) {
            idsToRecords[recordId]?.let { deletedIdsToRecords[recordId] = it }
            idsToRecords.remove(recordId)
            removeUpsertion(recordId)
            addDeletionChange(recordId)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Record> readRecord(
        recordType: KClass<T>,
        recordId: String
    ): ReadRecordResponse<T> {
        return ReadRecordResponse(idsToRecords[recordId.toRecordId(packageName)] as T)
    }

    /**
     * Returns records that match the attributes in a [ReadRecordsRequest].
     *
     * Features a simple paging implementation. Records must not be updated in between calls.
     *
     * @throws IllegalStateException if paging is requested.
     */
    @Suppress("UNCHECKED_CAST")
    public override suspend fun <T : Record> readRecords(
        request: ReadRecordsRequest<T>
    ): ReadRecordsResponse<T> {
        val startIndex = request.pageToken?.toIntOrNull() ?: 0
        val allRecords =
            idsToRecords
                .filterValues { record ->
                    record::class == request.recordType &&
                        record.isWithin(request.timeRangeFilter, clock) &&
                        (request.dataOriginFilter.isEmpty() ||
                            request.dataOriginFilter.contains(record.metadata.dataOrigin))
                }
                .values
                .map { record -> record as T }

        val recordsPending = allRecords.drop(startIndex)
        val hasMorePages = recordsPending.size > request.pageSize

        // Increment the token if there are more pages
        val nextPageToken =
            if (hasMorePages) {
                // Next page token
                (request.pageToken?.toLongOrNull() ?: 0) + request.pageSize
            } else {
                null
            }
        // Fake implementation
        return ReadRecordsResponse(
            records = recordsPending.take(request.pageSize),
            pageToken = nextPageToken?.toString()
        )
    }

    /** @throws IllegalStateException if no overrides are configured. */
    override suspend fun aggregate(request: AggregateRequest): AggregationResult {
        TODO("This function should be mocked in tests.")
    }

    /** @throws IllegalStateException if no overrides are configured. */
    override suspend fun aggregateGroupByDuration(
        request: AggregateGroupByDurationRequest
    ): List<AggregationResultGroupedByDuration> {
        TODO("This function should be mocked in tests.")
    }

    /** @throws IllegalStateException if no overrides are configured. */
    override suspend fun aggregateGroupByPeriod(
        request: AggregateGroupByPeriodRequest
    ): List<AggregationResultGroupedByPeriod> {
        TODO("This function should be mocked in tests.")
    }

    /**
     * Returns a fake token which contains the key to the next change. Used with [getChanges] to
     * track changes from the moment this function is called.
     */
    override suspend fun getChangesToken(request: ChangesTokenRequest): String {
        if (request.recordTypes.isEmpty()) {
            throw IllegalArgumentException("Record types must not be empty")
        }
        if (request.dataOriginFilters.isNotEmpty()) {
            throw UnsupportedOperationException(
                "Data origin filters are not supported in the " +
                    "fake. Use [StubResponse]s with [overrides.getChangesToken] and " +
                    "[overrides.getChanges] to set a response."
            )
        }
        val nextInstant = timeToChangesLastKey + 1
        val newToken = generateNewToken(nextInstant, request.recordTypes)
        tokens[newToken] = TokenInfo(recordTypes = request.recordTypes, time = nextInstant)
        return newToken
    }

    /** Set a particular token as expired. This is used to test the response of [getChanges]. */
    public fun expireToken(token: String) {
        val tokenInfo = tokens[token] ?: throw IllegalStateException("Token not found")
        tokens[token] = tokenInfo.copy(expired = true)
    }

    private fun generateNewToken(time: Long, recordTypes: Set<KClass<out Record>>): String {
        val recordTypesHash =
            recordTypes
                .mapNotNull { record ->
                    // Get a string representation of each record
                    RECORDS_TYPE_NAME_MAP.filterValues { it == record }.keys.firstOrNull()
                }
                .sorted() // Sort them alphabetically so that the order doesn't matter
                .takeIf { it.isNotEmpty() }
                ?.joinToString(",", prefix = "_") ?: ""

        return "$time$recordTypesHash"
    }

    override suspend fun getChanges(changesToken: String): ChangesResponse {
        // The token is related to a moment in time in [tokenToTime] and to a set of Record types
        // in [tokenToRecordTypes].
        val tokenInfo = tokens[changesToken] ?: throw IllegalStateException("Token not found")
        val timeInToken = tokenInfo.time
        val recordTypes = tokenInfo.recordTypes

        val changes =
            timeToChanges
                .filterKeys { key -> key >= timeInToken }
                .filterValues { change: Change ->
                    // Only return changes whose records are of the requested types
                    when (change) {
                        is UpsertionChange -> recordTypes.contains(change.record::class)
                        is DeletionChange -> {
                            val record = deletedIdsToRecords[change.recordId] ?: false
                            recordTypes.contains(record::class)
                        }
                        else -> throw NotImplementedError()
                    }
                }
                .values
        val hasMoreChanges = changes.size > pageSizeGetChanges
        val nextChangesToken =
            if (hasMoreChanges) {
                // Next page token
                generateNewToken(timeInToken + pageSizeGetChanges, recordTypes)
            } else {
                // Future changes token
                generateNewToken(timeToChangesLastKey + 1, recordTypes)
            }

        // Store metadata for new token
        tokens[nextChangesToken] = tokenInfo.copy(time = tokenInfo.time + pageSizeGetChanges)

        return ChangesResponse(
            changes.take(pageSizeGetChanges).toList(),
            hasMore = hasMoreChanges,
            changesTokenExpired = tokenInfo.expired,
            nextChangesToken = nextChangesToken
        )
    }

    private fun String.toRecordId(packageName: String): String {
        return "$packageName:$this"
    }

    private fun addDeletionChange(recordId: String) {
        timeToChanges[++timeToChangesLastKey] = DeletionChange(recordId)
    }

    private fun addUpsertionChange(updatedRecord: Record) {
        timeToChanges[++timeToChangesLastKey] = UpsertionChange(updatedRecord)
    }

    private fun removeUpsertion(recordId: String) {
        timeToChanges
            .filterValues { it is UpsertionChange && it.record.metadata.id == recordId }
            .keys
            .forEach { timeToChanges.remove(it) }
    }

    public companion object {
        /** Default package name used in [FakeHealthConnectClient]. */
        public const val DEFAULT_PACKAGE_NAME: String = "androidx.health.connect.test"
    }
}

private data class TokenInfo(
    val time: Long,
    val recordTypes: Set<KClass<out Record>>,
    val expired: Boolean = false
)
