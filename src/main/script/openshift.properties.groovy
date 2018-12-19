/*
 * Copyright (C) 2018 Red Hat, Inc.
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

def evaluate = { cmd ->
    def proc = cmd.execute()
    proc.waitFor()

    if (proc.exitValue()) {
        println "[ERROR] " + proc.getErrorStream().text
    }

    assert !proc.exitValue()
    proc.text.trim()
}

def domainProperty = "openshift.domain";
def registryProperty = "openshift.registry";

if (!session.userProperties[domainProperty] && !project.properties[domainProperty]) {
    project.properties.setProperty(domainProperty, evaluate('minishift ip') + '.nip.io')
    println("[INFO] Setting Openshift domain property: ${project.properties[domainProperty]}")
}

if (!session.userProperties[registryProperty] && !project.properties[registryProperty]) {
    project.properties.setProperty(registryProperty, evaluate('oc registry info'))
    println("[INFO] Setting Openshift registry property: ${project.properties[registryProperty]}")
}
