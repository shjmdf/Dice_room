package backend.playercard;

/*
 * 角色状态。
 *
 * 包含角色的各种状态，如重伤、昏迷、死亡、暂时性疯狂、永久性疯狂和不定性疯狂等。
*/
public class CharacterStatus {
    private boolean seriousInjury;
    private boolean unconscious;
    private boolean dead;
    private boolean temporaryInsanity;
    private boolean permanentInsanity;
    private boolean indefiniteInsanity;

    public boolean isSeriousInjury() {
        return seriousInjury;
    }

    public void setSeriousInjury(boolean seriousInjury) {
        this.seriousInjury = seriousInjury;
    }

    public boolean isUnconscious() {
        return unconscious;
    }

    public void setUnconscious(boolean unconscious) {
        this.unconscious = unconscious;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isTemporaryInsanity() {
        return temporaryInsanity;
    }

    public void setTemporaryInsanity(boolean temporaryInsanity) {
        this.temporaryInsanity = temporaryInsanity;
    }

    public boolean isPermanentInsanity() {
        return permanentInsanity;
    }

    public void setPermanentInsanity(boolean permanentInsanity) {
        this.permanentInsanity = permanentInsanity;
    }

    public boolean isIndefiniteInsanity() {
        return indefiniteInsanity;
    }

    public void setIndefiniteInsanity(boolean indefiniteInsanity) {
        this.indefiniteInsanity = indefiniteInsanity;
    }
}
