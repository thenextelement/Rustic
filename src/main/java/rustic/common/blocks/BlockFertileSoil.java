package rustic.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

import rustic.core.Rustic;

public class BlockFertileSoil extends BlockBase {

    public BlockFertileSoil() {
        super(Material.ground, "fertile_soil");
        setHardness(0.5F);
        setCreativeTab(Rustic.farmingTab);
        setStepSound(soundTypeGravel);
    }

    @Override
    public boolean isFertile(World world, int x, int y, int z) {
        return true;
    }

    @Override
    public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction,
        IPlantable plantable) {
        EnumPlantType plantType = plantable
            .getPlantType(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
        switch (plantType) {
            case Desert:
            case Crop:
            case Cave:
            case Plains:
            case Beach:
                return true;
            case Nether:
            case Water:
                return false;
            default:
                return super.canSustainPlant(world, x, y, z, direction, plantable);
        }
    }

}
