/*
Copyright (c) 2014, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
*/

package edu.colostate.cs.galileo.event;

import edu.colostate.cs.galileo.serialization.SerializationInputStream;
import edu.colostate.cs.galileo.serialization.SerializationOutputStream;

import java.io.IOException;

/**
 * Represents an event that is just a 'stub' -- doesn't actually do anything
 * other than serve as an identifier.  For instance, this might be used to
 * notify a service to start executing a particular task that doesn't take any
 * parameters.
 * <p>
 * This class can be extended to create multiple, unique stub events.
 */
public class StubEvent implements Event {

    public StubEvent() { }

    @Deserialize
    public StubEvent(SerializationInputStream in)
    throws IOException { }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException { }
}
