/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing.queries;

import com.eventsourcing.Model;

/**
 * Combines all standard queries into one:
 * <ul>
 *     <li>{@link LatestAssociatedEntryQuery}</li>
 * </ul>
 */
public interface ModelQueries extends Model, LatestAssociatedEntryQuery {}
