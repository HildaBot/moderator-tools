/*******************************************************************************
 * Copyright 2017 jamietech
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
 *******************************************************************************/
package ch.jamiete.hilda.moderatortools;

import java.io.Serializable;
import net.dv8tion.jda.core.entities.Member;

public class FlowMember implements Serializable {
    private static final long serialVersionUID = -1L;
    public String id;
    public String nickname;
    public String username;
    public String discriminator;

    /**
     * Instantiates an empty FlowMember. <b>Only use this where members are being loaded from disk.</b>
     */
    public FlowMember() {

    }

    public FlowMember(final Member member) {
        this.id = member.getUser().getId();
        this.nickname = member.getNickname();
        this.username = member.getUser().getName();
        this.discriminator = member.getUser().getDiscriminator();
    }

    public String getEffectiveName() {
        return this.nickname == null ? this.username : this.nickname;
    }

}
