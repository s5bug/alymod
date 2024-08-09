package tf.bug.alymod.monk;

public abstract class MonkStats {

    public abstract int getWeaponDamage(); // { return 93; }
    public abstract int getStrength(); // { return 1388; }
    public abstract int getDirectHit(); // { return 1088; }
    public abstract int getCriticalHit(); // { return 1241; }
    public abstract int getDetermination(); // { return 701; }
    public abstract int getSkillSpeed(); // { return 568; }

    public abstract int levelDetMod(); // { return 292; }
    public abstract int levelSubMod(); // { return 364; }
    public abstract int levelDivMod(); // { return 900; }
    public abstract int attackPowerMod(); // { return 130; }
    public abstract int jobStrengthMod(); // { return 110; }

    public final double getProbabilityDirectHit() {
        return Math.floorDiv(550 * (this.getDirectHit() - this.levelSubMod()), this.levelDivMod()) / 1000.0d;
    }

    public final double getProbabilityCriticalHit() {
        return (50 + Math.floorDiv(200 * (this.getCriticalHit() - this.levelSubMod()), this.levelDivMod())) / 1000.0d;
    }

    public final int getMultiplierWeaponDamage() {
        return this.getWeaponDamage() + Math.floorDiv(this.levelDetMod() * this.jobStrengthMod(), 1000);
    }

    public final double getMultiplierAttackPower() {
        return 1.0 + (Math.floorDiv(this.attackPowerMod() * (this.getStrength() - this.levelDetMod()), this.levelDetMod()) / 100.0d);
    }

    public final double getMultiplierDetermination() {
        return 1.0 + (Math.floorDiv(130 * (this.getDetermination() - this.levelDetMod()), this.levelDivMod()) / 1000.0d);
    }

    public final double getMultiplierCrit() {
        return 1.0 + ((400.0d + Math.floorDiv(200 * (this.getCriticalHit() - this.levelSubMod()), this.levelDivMod())) / 1000.0d);
    }

    public final double getMultiplierSkillSpeed() {
        return 1.0 - (Math.floorDiv(130 * (this.getSkillSpeed() - this.levelSubMod()), this.levelDivMod()) / 1000.0d);
    }

    public static final class Lv70 extends MonkStats {
        private Lv70() {}
        public static final Lv70 INSTANCE = new Lv70();

        @Override
        public int getWeaponDamage() {
            return 93;
        }

        @Override
        public int getStrength() {
            return 1388;
        }

        @Override
        public int getDirectHit() {
            return 1088;
        }

        @Override
        public int getCriticalHit() {
            return 1241;
        }

        @Override
        public int getDetermination() {
            return 701;
        }

        @Override
        public int getSkillSpeed() {
            return 568;
        }

        @Override
        public int levelDetMod() {
            return 292;
        }

        @Override
        public int levelSubMod() {
            return 364;
        }

        @Override
        public int levelDivMod() {
            return 900;
        }

        @Override
        public int attackPowerMod() {
            return 130;
        }

        @Override
        public int jobStrengthMod() {
            return 110;
        }
    }

}
