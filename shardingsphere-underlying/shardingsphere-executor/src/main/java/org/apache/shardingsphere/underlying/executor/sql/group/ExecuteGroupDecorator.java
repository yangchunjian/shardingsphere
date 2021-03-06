/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.underlying.executor.sql.group;

import org.apache.shardingsphere.sharding.spi.order.OrderedSPI;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import java.util.Collection;

/**
 * Execute group decorator.
 * 
 * @param <T> type of input value 
 */
public interface ExecuteGroupDecorator<T> extends OrderedSPI {
    
    /**
     * Decorate input groups.
     * 
     * @param inputGroups input groups to be decorated
     * @return decorated input groups.
     */
    Collection<InputGroup<T>> decorate(Collection<InputGroup<T>> inputGroups);
}
