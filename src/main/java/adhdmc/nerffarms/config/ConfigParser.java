package adhdmc.nerffarms.config;

import adhdmc.nerffarms.NerfFarms;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConfigParser {
    private static final HashSet<Material> standOnBlacklist = new HashSet<>();
    private static final HashSet<Material> insideBlacklist = new HashSet<>();
    private static final HashSet<EntityType> bypassList = new HashSet<>();
    private static final HashSet<CreatureSpawnEvent.SpawnReason> spawnReasonList = new HashSet<>();
    private static final HashSet<EntityDamageEvent.DamageCause> blacklistedDamageTypes = new HashSet<>();
    private static int maxDistance = 0;
    private static int errorCount = 0;
    private static int maxBlacklistedDamage = 100;
    private static boolean debug = false;

    public static void validateConfig() {
        //you're doing the best you can, NerfFarms.plugin.getConfig().
        //clear any set stuff.
        standOnBlacklist.clear();
        insideBlacklist.clear();
        bypassList.clear();
        spawnReasonList.clear();
        blacklistedDamageTypes.clear();
        maxDistance = 0;
        errorCount = 0;
        maxBlacklistedDamage = 100;
        List<String> standStringList = NerfFarms.plugin.getConfig().getStringList("blacklisted-below");
        List<String> inStringList = NerfFarms.plugin.getConfig().getStringList("blacklisted-in");
        List<String> bypassStringList = NerfFarms.plugin.getConfig().getStringList("bypass");
        List<String> spawnReasonStringList = NerfFarms.plugin.getConfig().getStringList("whitelisted-spawn-types");
        List<String> blacklistedDamageTypesList = NerfFarms.plugin.getConfig().getStringList("blacklisted-damage-types");
        int maxDistanceInt = NerfFarms.plugin.getConfig().getInt("max-distance");
        int maxBlacklistedDamageConfig = NerfFarms.plugin.getConfig().getInt("max-blacklisted-damage-percent");
        debug = NerfFarms.plugin.getConfig().getBoolean("debug");

        // Assemble the Stand On BlackList
        for (String type : standStringList) {
            Material materialType = Material.matchMaterial(type);
            if (materialType != null && materialType.isBlock()) {
                standOnBlacklist.add(materialType);
            } else {
                NerfFarms.plugin.getLogger().warning(type + " is not a valid block for mobs to stand on, please choose another.");
                errorCount = errorCount + 1;
            }
        }

        // Assemble the Inside BlackList
        for (String type : inStringList) {
            Material materialType = Material.matchMaterial(type);
            if (materialType != null && materialType.isBlock()) {
                insideBlacklist.add(materialType);
            } else {
                NerfFarms.plugin.getLogger().warning(type + " is not a valid block for mobs to be inside, please choose another.");
                errorCount = errorCount + 1;
            }
        }

        // Mob Bypass
        for (String type : bypassStringList) {
            if (type == null || type.equalsIgnoreCase("")) break;
            try {
                EntityType.valueOf(type.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                NerfFarms.plugin.getLogger().warning(type + " is not a valid entity to blacklist. Please choose another.");
                errorCount = errorCount + 1;
                continue;
            }
            EntityType entityType = EntityType.valueOf(type.toUpperCase(Locale.ENGLISH));
            if (entityType.isAlive()) {
                bypassList.add(entityType);
            } else {
                NerfFarms.plugin.getLogger().warning(type + " is not a valid entity for bypass. Please choose another.");
                errorCount = errorCount + 1;
            }
        }

        // Generate Spawn Reasons
        for (String type : spawnReasonStringList) {
            try {
                CreatureSpawnEvent.SpawnReason.valueOf(type.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                NerfFarms.plugin.getLogger().warning(type + " is not a valid spawn reason. Please check that you have entered this correctly.");
                errorCount = errorCount + 1;
                continue;
            }
            spawnReasonList.add(CreatureSpawnEvent.SpawnReason.valueOf(type.toUpperCase(Locale.ENGLISH)));
        }
        // Generate Environmental Causes
        for (String type : blacklistedDamageTypesList) {
            try {
                EntityDamageEvent.DamageCause.valueOf(type);
            } catch (IllegalArgumentException exception) {
                NerfFarms.plugin.getLogger().warning(type + " is not a valid damage type. Please check that you have entered this correctly.");
                errorCount = errorCount + 1;
                continue;
            }
            blacklistedDamageTypes.add(EntityDamageEvent.DamageCause.valueOf(type));
        }
        // Determine modType
        ModType.setModType();

        // Determine Distance
        if (!(maxDistanceInt > 1 && maxDistanceInt < 120)) {
            NerfFarms.plugin.getLogger().warning("Max player distance must be between 1 and 120, setting distance to 20");
            errorCount = errorCount + 1;
            maxDistance = 20;
        } else {
            maxDistance = maxDistanceInt;
        }

        // Determine Percent Damage from Environment
        if (maxBlacklistedDamageConfig <= 0 || maxBlacklistedDamageConfig > 100) {
            NerfFarms.plugin.getLogger().warning("Percent damage from Environment must be between 1 and 100, setting to 100");
            errorCount = errorCount + 1;
            maxBlacklistedDamage = 100;
        } else {
            maxBlacklistedDamage = maxBlacklistedDamageConfig;
        }
        ConfigToggle.reloadToggles();
    }

    public static Set<Material> getStandOnBlackList() {
        return Collections.unmodifiableSet(standOnBlacklist);
    }

    public static Set<Material> getInsideBlackList() {
        return Collections.unmodifiableSet(insideBlacklist);
    }

    public static Set<EntityType> getBypassList() {
        return Collections.unmodifiableSet(bypassList);
    }

    public static Set<CreatureSpawnEvent.SpawnReason> getSpawnReasonList() {
        return Collections.unmodifiableSet(spawnReasonList);
    }

    public static Set<EntityDamageEvent.DamageCause> getblacklistedDamageTypesSet() {
        return Collections.unmodifiableSet(blacklistedDamageTypes);
    }

    /**
     * Returns the modType setting, defining if EXP and/or Drops should be cleared.
     *
     * @return ModType enum, defaults to NEITHER
     */

    public static int getMaxDistance() {
        return maxDistance;
    }

    public static int getErrorCount() {
        return errorCount;
    }

    public static int getMaxBlacklistedDamage() {
        return maxBlacklistedDamage;
    }

    public static boolean isDebug() {
        return debug;
    }

}
