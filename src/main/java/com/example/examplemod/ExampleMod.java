package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";

    // Настройки (потом вынеси в конфиг)
    private static final double STIFFNESS = 0.12;
    private static final double DAMPING = 0.35;
    private static final double MAX_ACCEL = 0.08;
    private static final double REACH = 0.8;

    public ExampleMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(ExampleMod::onLevelTick);
    }

    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        double t = level.getGameTime() / 20.0;

        for (Entity e : level.getAllEntities()) {
            if (!(e instanceof ItemEntity || e instanceof Boat || e instanceof LivingEntity)) continue;
            if (e.isSpectator() || !e.isInWater()) continue;

            double surface = surfaceY(level, e.blockPosition());
            if (Double.isNaN(surface)) continue;

            double waterLine = surface + WaveMath.height(e.getX(), e.getZ(), t);
            double restDepth = e.getBbHeight() * 0.7;
            double diff = (waterLine - restDepth) - e.getY();

            if (Math.abs(diff) > REACH) continue;

            Vec3 v = e.getDeltaMovement();
            double acc = Mth.clamp(STIFFNESS * diff - DAMPING * v.y, -MAX_ACCEL, MAX_ACCEL);
            e.setDeltaMovement(v.x, v.y + acc, v.z);
            e.hurtMarked = true; // синхронизировать скорость с клиентом
        }
    }

    /** Верхняя граница воды над позицией, NaN если позиция не в воде. */
    private static double surfaceY(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos p = start.mutable();
        if (!level.getFluidState(p).is(FluidTags.WATER)) return Double.NaN;
        for (int i = 0; i < 32; i++) {
            p.move(Direction.UP);
            if (!level.getFluidState(p).is(FluidTags.WATER)) {
                p.move(Direction.DOWN);
                break;
            }
        }
        return p.getY() + level.getFluidState(p).getOwnHeight();
    }
}
