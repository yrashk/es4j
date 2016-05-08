/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package boguspackage;

import com.eventsourcing.Command;
import com.eventsourcing.Event;
import com.eventsourcing.Repository;

import java.util.stream.Stream;

public class BogusCommand extends Command<String> {

    @Override
    public Stream<Event> events(Repository repository) throws Exception {
        return Stream.of(new BogusEvent());
    }

    @Override
    public String onCompletion() {
        return "bogus";
    }
}
