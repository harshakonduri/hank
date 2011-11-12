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

package com.rapleaf.hank.storage.curly;

import com.rapleaf.hank.coordinator.Domain;
import com.rapleaf.hank.coordinator.DomainVersion;
import com.rapleaf.hank.storage.IncrementalPartitionUpdater;
import com.rapleaf.hank.storage.PartitionRemoteFileOps;
import com.rapleaf.hank.storage.cueball.Cueball;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;

public class CurlyPartitionUpdater extends IncrementalPartitionUpdater {

  public CurlyPartitionUpdater(Domain domain, PartitionRemoteFileOps partitionRemoteFileOps) {
    super(domain, partitionRemoteFileOps);
  }

  @Override
  protected Integer detectCurrentVersionNumber() throws IOException {
    throw new NotImplementedException();
  }

  // TODO: determining the parent domain version should be based on DomainVersion metadata instead
  @Override
  protected DomainVersion getParentDomainVersion(DomainVersion domainVersion) throws IOException {
    if (partitionRemoteFileOps.exists(Cueball.getName(domainVersion.getVersionNumber(), true))
        && partitionRemoteFileOps.exists(Curly.getName(domainVersion.getVersionNumber(), true))) {
      // Base files exists, there is no parent
      return null;
    } else if (partitionRemoteFileOps.exists(Cueball.getName(domainVersion.getVersionNumber(), false))
               && partitionRemoteFileOps.exists(Curly.getName(domainVersion.getVersionNumber(), false))) {
      // Delta files exists, the parent is just the previous version based on version number
      int versionNumber = domainVersion.getVersionNumber();
      if (versionNumber <= 0) {
        return null;
      } else {
        DomainVersion result = domain.getVersionByNumber(versionNumber - 1);
        if (result == null) {
          throw new IOException("Failed to find version numbered " + (versionNumber - 1)
              + " of domain " + domain
              + " which was determined be the parent of domain version " + domainVersion);
        }
        return result;
      }
    } else {
      throw new IOException("Failed to determine parent version of domain version: " + domainVersion);
    }
  }
}
