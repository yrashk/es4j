/**
 * Copyright (c) 2016, All Contributors (see CONTRIBUTORS file)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

/**
 * Protocol is a convention interface for implementing so called
 * Domain Protocols.
 * <p>
 * The idea behind protocols is to re-use "polymorphic" events to
 * perform common operations on various types of entities.
 * <p>
 * Consider, for example, that we have models called Product, Widget
 * and Company. All of these models have a name.
 * <p>
 * Instead of assigning name or renaming each of these models in individual
 * command/event pairs (RenameProduct/ProductRenamed, RenameWidget/WidgetRenamed,
 * RenameCompany/CompanyRemained) and having similar yet different name retrieval methods
 * in the respective model, the following approach is used:
 * <p>
 * There is only one command/event pair (Rename/NameChanged) that takes and stores a "polymorphic"
 * reference to any model (its UUID) and a new name. Now, since renaming any supported model
 * is done the same way, we can implement a common protocol for name retrieval:
 * <p>
 * <pre>
 * <code>
 *
 * public interface NameProtocol extends Protocol {
 *     public String name() {
 *         try (ResultSet&lt;EntityHandle&lt;NameChanged&gt;&gt; resultSet =
 *              repository.query(NameChanged.class, equal(NameChanged.REFERENCE_ID, getId()),
 *                               queryOptions(orderBy(descending(attribute)),
 *                                            applyThresholds(threshold(EngineThresholds.INDEX_ORDERING_SELECTIVITY, 0.5))))) {
 *              if (resultSet.isEmpty()) {
 *                  return null;
 *              }
 *              return resultSet.iterator().next().getOptional().getOptional().name();
 *        }
 *     }
 * }
 *
 * </code>
 * </pre>
 * <p>
 * <p>
 * The above protocol implements a {@code name()} function that retrieves the latestAssociatedEntity {@code NameChange} for the particular
 * model referenced by its UUID ({@code getId()}).
 * <p>
 * Now, all we have to do is to make every model implement this interface:
 * <p>
 * <pre>
 * <code>
 *
 * public class Product implements Model, NameProtocol { ... }
 * public class Widget implements Model, NameProtocol { ... }
 * public class Company implements Model, NameProtocol { ... }
 *
 * </code>
 * </pre>
 */
public interface Protocol extends Model {
}
