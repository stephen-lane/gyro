/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.lang.ast.value;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gyro.lang.ast.Node;
import gyro.lang.ast.NodeVisitor;
import gyro.lang.ast.PairNode;
import gyro.parser.antlr4.GyroParser;

public class MapNode extends Node {

    private List<PairNode> entries;

    public MapNode(List<PairNode> entries) {
        super(null);

        this.entries = ImmutableList.copyOf(Preconditions.checkNotNull(entries));
    }

    @SuppressWarnings("unchecked")
    public MapNode(GyroParser.MapContext context) {
        super(Preconditions.checkNotNull(context));

        this.entries = (List) Node.create(context.pair());
    }

    public List<PairNode> getEntries() {
        return entries;
    }

    @Override
    public <C, R, X extends Throwable> R accept(NodeVisitor<C, R, X> visitor, C context) throws X {
        return visitor.visitMap(this, context);
    }

}
