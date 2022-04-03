package protosky.gen;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.structure.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.feature.ConfiguredStructureFeatures;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import protosky.mixins.StructurePieceAccessor;

public class StructureHelper
{
    public static BlockPos getBlockInStructurePiece(StructurePiece piece, int x, int y, int z) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        return new BlockPos(access.invokeApplyXTransform(x, z), access.invokeApplyYTransform(y), access.invokeApplyZTransform(x, z));
    }

    public static void setBlockInStructure(StructurePiece piece, ProtoChunk chunk, BlockState state, int x, int y, int z) {
        StructurePieceAccessor access = (StructurePieceAccessor) piece;
        BlockPos pos = getBlockInStructurePiece(piece, x, y, z);

        if (piece.getBoundingBox().contains(pos)) {
            BlockMirror mirror = access.getMirror();

            if (mirror != BlockMirror.NONE) {
                state = state.mirror(mirror);
            }

            BlockRotation rotation = piece.getRotation();

            if (rotation != BlockRotation.NONE) {
                state = state.rotate(rotation);
            }

            setBlockInChunk(chunk, pos, state);
        }
    }
    
    public static void setBlockInChunk(ProtoChunk chunk, BlockPos pos, BlockState state) {
        if (chunk.getPos().equals(new ChunkPos(pos))) {
            chunk.setBlockState(pos, state, false);
        }
    }

    public static void generatePillars(ProtoChunk chunk, StructureWorldAccess world, EnderDragonFight fight) {
        for (EndSpikeFeature.Spike spike : EndSpikeFeature.getSpikes(world)) {
            if (spike.isInChunk(new BlockPos(spike.getCenterX(), 45, spike.getCenterZ()))) {
                PillarHelper.generateSpike(chunk, spike);
            }
        }
    }
    
    public static void processStronghold(ProtoChunk chunk, WorldAccess world) {
        for (long startPosLong : chunk.getStructureReferences(ConfiguredStructureFeatures.STRONGHOLD.value())) {
            ChunkPos startPos = new ChunkPos(startPosLong);
            ProtoChunk startChunk = (ProtoChunk) world.getChunk(startPos.x, startPos.z, ChunkStatus.STRUCTURE_STARTS);
            StructureStart stronghold = startChunk.getStructureStart(ConfiguredStructureFeatures.STRONGHOLD.value());

            ChunkPos pos = chunk.getPos();
            BlockBox posBox = new BlockBox(pos.getStartX(), world.getBottomY(), pos.getStartZ(), pos.getEndX(), world.getTopY(), pos.getEndZ());

            if (stronghold != null && isIntersecting(stronghold, posBox)) {
                for (StructurePiece piece : stronghold.getChildren()) {
                    if (piece.getBoundingBox().intersectsXZ(pos.getStartX(), pos.getStartZ(), pos.getEndX(), pos.getEndZ()) && piece instanceof StrongholdGenerator.PortalRoom) {
                        generateStrongholdPortalRoom(chunk, (StrongholdGenerator.PortalRoom) piece);
                    }
                }
            }
        }
    }

    private static boolean isIntersecting(StructureStart stronghold, BlockBox posBox) {
        StructurePiecesHolder structurePiecesHolder = new StructurePiecesCollector();

        if (stronghold != null) {
            for (StructurePiece piece : stronghold.getChildren()) {
                structurePiecesHolder.addPiece(piece);
            }

            return structurePiecesHolder.getIntersecting(posBox) != null;
        }

        return false;
    }

    public static void generateStrongholdPortalRoom(ProtoChunk chunk, StrongholdGenerator.PortalRoom room) {
        BlockState northFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.NORTH);
        BlockState southFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.SOUTH);
        BlockState eastFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.EAST);
        BlockState westFrame = Blocks.END_PORTAL_FRAME.getDefaultState().with(EndPortalFrameBlock.FACING, Direction.WEST);

        setBlockInStructure(room, chunk, northFrame, 4, 3, 8);
        setBlockInStructure(room, chunk, northFrame, 5, 3, 8);
        setBlockInStructure(room, chunk, northFrame, 6, 3, 8);
        setBlockInStructure(room, chunk, southFrame, 4, 3, 12);
        setBlockInStructure(room, chunk, southFrame, 5, 3, 12);
        setBlockInStructure(room, chunk, southFrame, 6, 3, 12);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 9);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 10);
        setBlockInStructure(room, chunk, eastFrame, 3, 3, 11);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 9);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 10);
        setBlockInStructure(room, chunk, westFrame, 7, 3, 11);
    }
}
