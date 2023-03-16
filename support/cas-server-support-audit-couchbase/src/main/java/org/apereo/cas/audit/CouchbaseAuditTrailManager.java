package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import com.couchbase.client.java.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link CouchbaseAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 * @deprecated Since 7.0.0
 */
@Setter
@RequiredArgsConstructor
@Deprecated(since = "7.0.0")
public class CouchbaseAuditTrailManager extends AbstractAuditTrailManager {
    private final CouchbaseClientFactory couchbase;

    private final StringSerializer<AuditActionContext> serializer;

    public CouchbaseAuditTrailManager(final CouchbaseClientFactory couchbase,
                                      final StringSerializer<AuditActionContext> serializer,
                                      final boolean asynchronous) {
        this(couchbase, serializer);
        this.asynchronous = asynchronous;
    }

    @Override
    @SuppressWarnings("JavaUtilDate")
    public Set<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDate) whereClause.get(WhereClauseFields.DATE);
        val parameters = JsonObject.create().put("whenActionWasPerformed", DateTimeUtils.dateOf(localDate).getTime());
        val queryBuilder = new StringBuilder("whenActionWasPerformed >= $whenActionWasPerformed");
        if (whereClause.containsKey(WhereClauseFields.PRINCIPAL)) {
            parameters.put("principal", whereClause.get(WhereClauseFields.PRINCIPAL).toString());
            queryBuilder.append(" and principal = $principal");
        }
        val result = couchbase.select(queryBuilder.toString(), Optional.of(parameters));

        return result.rowsAsObject()
            .stream()
            .map(row -> {
                val json = row.toString();
                val bucket = JsonObject.fromJson(json).getObject(couchbase.getBucket());
                return new AuditActionContext(bucket.getString("principal"),
                    bucket.getString("resourceOperatedUpon"),
                    bucket.getString("actionPerformed"),
                    bucket.getString("applicationCode"),
                    new Date(bucket.getArray("whenActionWasPerformed").getLong(1)),
                    bucket.getString("clientIpAddress"),
                    bucket.getString("serverIpAddress"),
                    bucket.getString("userAgent"));
            })
            .collect(Collectors.toSet());
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        FunctionUtils.doUnchecked(__ -> {
            try (val stringWriter = new StringWriter()) {
                this.serializer.to(stringWriter, audit);
                this.couchbase.bucketUpsertDefaultCollection(stringWriter.toString());
            }
        });
    }
}
