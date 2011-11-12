/**
 *  Copyright 2011 Rapleaf
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.rapleaf.hank.storage;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HdfsPartitionRemoteFileOps implements PartitionRemoteFileOps {

  private final String remoteDomainRoot;
  private final int partitionNumber;
  private final FileSystem fs;

  public HdfsPartitionRemoteFileOps(String remoteDomainRoot,
                                    int partitionNumber) throws IOException {
    this.remoteDomainRoot = remoteDomainRoot;
    this.partitionNumber = partitionNumber;
    this.fs = FileSystem.get(new Configuration());
  }

  @Override
  public boolean exists(String relativeFilePath) throws IOException {
    return fs.exists(new Path(getAbsolutePath(relativeFilePath)));
  }

  private String getAbsolutePath(String relativeFilePath) {
    return remoteDomainRoot + "/" + relativeFilePath;
  }
}
