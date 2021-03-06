/*
 * Copyright 2020 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.decaton.processor.runtime;

import static java.util.Collections.emptySet;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.linecorp.decaton.processor.runtime.AssignmentManager.AssignmentConfig;
import com.linecorp.decaton.processor.runtime.AssignmentManager.AssignmentStore;

public class AssignmentManagerTest {
    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Captor
    private ArgumentCaptor<Map<TopicPartition, AssignmentConfig>> captor;

    @Mock
    private AssignmentStore store;

    private AssignmentManager assignManager;

    @Before
    public void setUp() {
        assignManager = new AssignmentManager(store);
    }

    private static TopicPartition tp(int partition) {
        return new TopicPartition("topic", partition);
    }

    @Test
    public void testAssign() {
        doReturn(emptySet()).when(store).assignedPartitions();
        List<TopicPartition> partitions = Arrays.asList(tp(1), tp(2), tp(3));
        assignManager.assign(partitions);

        verify(store, times(1)).addPartitions(captor.capture());
        HashSet<TopicPartition> newAssign = new HashSet<>(partitions);
        assertEquals(newAssign, captor.getValue().keySet());
        verify(store, never()).removePartition(any());

        doReturn(newAssign).when(store).assignedPartitions();
        partitions = Arrays.asList(tp(2), tp(3), tp(4), tp(5));
        assignManager.assign(partitions);

        verify(store, times(2)).addPartitions(captor.capture());
        assertEquals(new HashSet<>(Arrays.asList(tp(4), tp(5))), captor.getValue().keySet());
        verify(store, never()).removePartition(new HashSet<>(Arrays.asList(tp(1))));
    }
}
