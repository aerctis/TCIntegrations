package tcintegrations.items.modifiers.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.module.ModuleHookMap.Builder;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ToolAttackUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;

import tcintegrations.items.modifiers.traits.ManaModifier;
import tcintegrations.util.BotaniaHelper;

public class TerraModifier extends ManaModifier implements MeleeHitModifierHook {

    private static final int MANA_PER_DAMAGE = 100;

    @Override
    protected void registerHooks(Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.MELEE_HIT);
    }

    @Override
    public int getManaPerDamage(ServerPlayer sp) {
        return BotaniaHelper.getManaPerDamageBonus(sp, MANA_PER_DAMAGE);
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        // Ensure the attacker is a server-side player and the target is a living entity.
        if (context.getAttacker() instanceof ServerPlayer player && context.getLivingTarget() != null) {
            LivingEntity target = context.getLivingTarget();
            ItemStack stack = player.getMainHandItem();
            int manaCost = getManaPerDamage(player) * 2;

            // Check if the attack is fully charged and if the player has enough mana.
            if (player.getAttackStrengthScale(0F) == 1.0F && ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, true)) {
                DamageSource source = player.level().damageSources().magic();
                source = source.bypassArmor();

                // Play the sound effect at the player's location.
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.terraBlade, SoundSource.PLAYERS, 1.0F, 1.0F);

                // Directly apply 7.0F damage to the target using the vanilla hurt method.
                target.hurt(source, 7.0F);
            }
        }
    }

}
