package ch.jamiete.hilda.moderatortools;

import net.dv8tion.jda.core.entities.Member;

public class FlowMember {
    public String id;
    public String nickname;
    public String username;
    public String discriminator;

    /**
     * Instantiates an empty FlowMember. <b>Only use this where members are being loaded from disk.</b>
     */
    public FlowMember() {

    }

    public FlowMember(Member member) {
        this.id = member.getUser().getId();
        this.nickname = member.getNickname();
        this.username = member.getUser().getName();
        this.discriminator = member.getUser().getDiscriminator();
    }

    public String getEffectiveName() {
        return this.nickname == null ? this.username : this.nickname;
    }

}
