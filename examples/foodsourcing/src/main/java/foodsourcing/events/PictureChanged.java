/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.events;

import com.eventsourcing.StandardEvent;
import com.eventsourcing.annotations.Index;
import com.eventsourcing.hlc.HybridTimestamp;
import com.eventsourcing.index.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import foodsourcing.Picture;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.Accessors;

import java.util.UUID;

import static com.eventsourcing.index.IndexEngine.IndexFeature.*;

@Value
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
public class PictureChanged extends StandardEvent implements Picture {
    private UUID reference;
    private String contentType;
    private byte[] picture;

    @Index
    public static SimpleAttribute<PictureChanged, UUID> REFERENCE_ID = new SimpleAttribute<PictureChanged, UUID>("referenceId") {
        @Override public UUID getValue(PictureChanged object, QueryOptions queryOptions) {
            return object.reference();
        }
    };

    @Index({EQ, LT, GT})
    public static SimpleAttribute<PictureChanged, HybridTimestamp> TIMESTAMP =
            new SimpleAttribute<PictureChanged, HybridTimestamp>("timestamp") {
                @Override public HybridTimestamp getValue(PictureChanged object, QueryOptions queryOptions) {
                    return object.timestamp();
                }
            };
}
