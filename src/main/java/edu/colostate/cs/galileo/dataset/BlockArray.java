/*
Copyright (c) 2013, Colorado State University
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

package edu.colostate.cs.galileo.dataset;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Iterator;

import edu.colostate.cs.galileo.serialization.ByteSerializable;
import edu.colostate.cs.galileo.serialization.SerializationInputStream;
import edu.colostate.cs.galileo.serialization.SerializationOutputStream;

public class BlockArray implements ByteSerializable, Iterable<FileBlock> {
    private ArrayList<FileBlock> blocks =
        new ArrayList<FileBlock>();

    public BlockArray() {

    }

    public void add(FileBlock block) {
        blocks.add(block);
    }

    public int size() {
        return blocks.size();
    }

    @Override
    public Iterator<FileBlock> iterator() {
        return blocks.iterator();
    }

    public BlockArray(SerializationInputStream in)
    throws IOException {
        int numBlocks = in.readInt();
        for (int i = 0; i < numBlocks; ++i) {
            FileBlock block = new FileBlock(in);
            blocks.add(block);
        }
    }

    @Override
    public void serialize(SerializationOutputStream out)
    throws IOException {
        out.writeInt(blocks.size());
        for (FileBlock block : blocks) {
            block.serialize(out);
        }
    }
}
