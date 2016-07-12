/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package foodsourcing.protocols;

import com.eventsourcing.Protocol;
import com.eventsourcing.queries.ModelQueries;
import foodsourcing.Picture;
import foodsourcing.events.PictureChanged;

public interface PictureProtocol extends Protocol, ModelQueries {
    default Picture picture() {
        PictureChanged pictureChanged = latestAssociatedEntity(PictureChanged.class,
                                                               PictureChanged.REFERENCE_ID, PictureChanged.TIMESTAMP)
                .orElse(new PictureChanged(getId(), "application/octet-stream", new byte[]{}));

        return pictureChanged;
    }
}
