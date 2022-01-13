package draylar.identity.mixin;

import draylar.identity.Identity;
import draylar.identity.api.IdentityGranting;
import draylar.identity.api.PlayerIdentity;
import draylar.identity.api.platform.IdentityConfig;
import draylar.identity.mixin.accessor.LivingEntityAccessor;
import draylar.identity.registry.EntityTags;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    protected abstract int getNextAirOnLand(int air);

    @Shadow
    public abstract boolean hasStatusEffect(StatusEffect effect);

    protected LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "onDeath",
            at = @At("RETURN")
    )
    private void onDeath(DamageSource source, CallbackInfo ci) {
        Entity attacker = source.getAttacker();
        EntityType<?> thisType = this.getType();

        // check if attacker is a player to grant identity
        if (attacker instanceof PlayerEntity) {
            IdentityGranting.grantByAttack((PlayerEntity) attacker, thisType);
        }
    }

    @Redirect(
            method = "baseTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setAir(I)V", ordinal = 2)
    )
    private void cancelAirIncrement(LivingEntity livingEntity, int air) {
        // Aquatic creatures should not regenerate breath on land
        if ((Object) this instanceof PlayerEntity player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                if (Identity.isAquatic(identity)) {
                    return;
                }
            }
        }

        this.setAir(this.getNextAirOnLand(this.getAir()));
    }

    @Redirect(
            method = "travel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 0)
    )
    private boolean slowFall(LivingEntity livingEntity, StatusEffect effect) {
        if ((Object) this instanceof PlayerEntity player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                if (!this.isSneaking() && EntityTags.SLOW_FALLING.contains(identity.getType())) {
                    return true;
                }
            }
        }

        return this.hasStatusEffect(StatusEffects.SLOW_FALLING);
    }

    @ModifyVariable(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z", ordinal = 1), ordinal = 0)
    public float applyWaterCreatureSwimSpeedBoost(float j) {
        if ((Object) this instanceof PlayerEntity player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            // Apply 'Dolphin's Grace' status effect benefits if the player's Identity is a water creature
            if (identity instanceof WaterCreatureEntity) {
                return .96f;
            }
        }

        return j;
    }

    @Inject(
            method = "handleFallDamage",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            LivingEntity identity = PlayerIdentity.getIdentity(player);

            if (identity != null) {
                boolean takesFallDamage = identity.handleFallDamage(fallDistance, damageMultiplier, damageSource);
                int damageAmount = ((LivingEntityAccessor) identity).callComputeFallDamage(fallDistance, damageMultiplier);

                if (takesFallDamage && damageAmount > 0) {
                    this.playSound(((LivingEntityAccessor) identity).callGetFallSound(damageAmount), 1.0F, 1.0F);
                    ((LivingEntityAccessor) identity).callPlayBlockFallSound();
                    this.damage(DamageSource.FALL, (float) damageAmount);
                    cir.setReturnValue(true);
                } else {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(
            method = "hasStatusEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void returnHasNightVision(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            if (effect.equals(StatusEffects.NIGHT_VISION)) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // Apply 'Night Vision' status effect to player if they are a Bat
                if (identity instanceof BatEntity) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Inject(
            method = "getStatusEffect",
            at = @At("HEAD"),
            cancellable = true
    )
    private void returnNightVisionInstance(StatusEffect effect, CallbackInfoReturnable<StatusEffectInstance> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            if (effect.equals(StatusEffects.NIGHT_VISION)) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                // Apply 'Night Vision' status effect to player if they are a Bat
                if (identity instanceof BatEntity) {
                    cir.setReturnValue(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 100000, 0, false, false));
                }
            }
        }
    }

    @Inject(
            method = "getMaxHealth",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyMaxHealth(CallbackInfoReturnable<Float> cir) {
        if (IdentityConfig.getInstance().scalingHealth()) {
            if ((Object) this instanceof PlayerEntity player) {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                if (identity != null) {
                    cir.setReturnValue(identity.getMaxHealth());
                }
            }
        }
    }

    @Inject(method = "hurtByWater", at = @At("HEAD"), cancellable = true)
    protected void identity_hurtByWater(CallbackInfoReturnable<Boolean> cir) {
        if((LivingEntity) (Object) this instanceof PlayerEntity player) {
            LivingEntity entity = PlayerIdentity.getIdentity(player);

            if(entity != null) {
                cir.setReturnValue(entity.hurtByWater());
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "getEyeHeight", cancellable = true)
    public void getEyeHeight(EntityPose pose, EntityDimensions dimensions, CallbackInfoReturnable<Float> cir) {
        if((LivingEntity) (Object) this instanceof PlayerEntity player) {

            // this is cursed
            try {
                LivingEntity identity = PlayerIdentity.getIdentity(player);

                if(identity != null) {
                    cir.setReturnValue(((LivingEntityAccessor) identity).callGetEyeHeight(pose, dimensions));
                }

            } catch (Exception ignored) {

            }
        }
    }

    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    protected void identity_canBreatheInWater(CallbackInfoReturnable<Boolean> cir) {
        // NO-OP
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "setNearbySongPlaying", at = @At("RETURN"))
    protected void identity_setNearbySongPlaying(BlockPos songPosition, boolean playing, CallbackInfo ci) {
        // NO-OP
    }

    @Inject(method = "isUndead", at = @At("HEAD"), cancellable = true)
    protected void identity_isUndead(CallbackInfoReturnable<Boolean> cir) {
        // NO-OP
    }

    @Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
    protected void identity_canWalkOnFluid(Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        // NO-OP
    }

    @Inject(
            method = "isClimbing",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void identity_allowSpiderClimbing(CallbackInfoReturnable<Boolean> cir) {
        // NO-OP
    }
}
