package draylar.identity.impl.variant;

import draylar.identity.api.variant.TypeProvider;
import draylar.identity.mixin.accessor.FoxEntityAccessor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class FoxTypeProvider extends TypeProvider<FoxEntity> {

    @Override
    public int getVariantData(FoxEntity entity) {
        return entity.getFoxType().getId();
    }

    @Override
    public FoxEntity create(EntityType<FoxEntity> type, World world, int data) {
        FoxEntity fox = new FoxEntity(type, world);
        ((FoxEntityAccessor) fox).callSetType(FoxEntity.Type.fromId(data));
        return null;
    }

    @Override
    public int getFallbackData() {
        return FoxEntity.Type.RED.getId();
    }

    @Override
    public int getRange() {
        return FoxEntity.Type.values().length - 1;
    }

    @Override
    public Text modifyText(FoxEntity entity, TranslatableText text) {
        return new LiteralText(formatTypePrefix(FoxEntity.Type.fromId(getVariantData(entity)).getKey()) + " ").append(text);
    }
}
