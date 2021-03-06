/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.giraph.hive.input.vertex;

import org.apache.giraph.hive.common.HiveProfiles;
import org.apache.giraph.io.VertexInputFormat;
import org.apache.giraph.io.VertexReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.facebook.giraph.hive.impl.input.HiveApiRecordReader;
import com.facebook.giraph.hive.input.HiveApiInputFormat;

import java.io.IOException;
import java.util.List;

/**
 * {@link VertexInputFormat} for reading vertices from Hive.
 *
 * @param <I> Vertex id
 * @param <V> Vertex value
 * @param <E> Edge value
 * @param <M> Message data
 */
public class HiveVertexInputFormat<I extends WritableComparable,
    V extends Writable, E extends Writable, M extends Writable>
    extends VertexInputFormat<I, V, E, M> {
  /** Underlying Hive InputFormat used */
  private final HiveApiInputFormat hiveInputFormat;

  /**
   * Create vertex input format
   */
  public HiveVertexInputFormat() {
    hiveInputFormat = new HiveApiInputFormat();
    hiveInputFormat.setMyProfileId(HiveProfiles.VERTEX_INPUT_PROFILE_ID);
  }

  @Override
  public List<InputSplit> getSplits(JobContext context, int minSplitCountHint)
    throws IOException, InterruptedException {
    return hiveInputFormat.getSplits(context);
  }

  @Override
  public VertexReader<I, V, E, M> createVertexReader(InputSplit split,
      TaskAttemptContext context) throws IOException {
    Configuration conf = context.getConfiguration();

    HiveVertexReader reader = new HiveVertexReader();
    reader.setTableSchema(hiveInputFormat.getTableSchema(conf));

    HiveApiRecordReader baseReader;
    try {
      baseReader = hiveInputFormat.createRecordReader(split, context);
    } catch (InterruptedException e) {
      throw new IOException("Could not create vertex reader", e);
    }

    reader.setHiveRecordReader(baseReader);
    return reader;
  }
}
