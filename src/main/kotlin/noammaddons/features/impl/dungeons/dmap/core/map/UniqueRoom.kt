package noammaddons.features.impl.dungeons.dmap.core.map

import noammaddons.features.impl.dungeons.dmap.core.DungeonMapConfig
import noammaddons.features.impl.dungeons.dmap.handlers.DungeonInfo


class UniqueRoom(arrX: Int, arrY: Int, room: Room) {
    val name = room.data.name
    private var topLeft = Pair(arrX, arrY)
    private var center = Pair(arrX, arrY)
    var mainRoom = room
    val tiles = mutableListOf(room)

    init {
        DungeonInfo.cryptCount += room.data.crypts
        DungeonInfo.secretCount += room.data.secrets
        when (room.data.type) {
            RoomType.TRAP -> DungeonInfo.trapType = room.data.name.split(" ")[0]
            //RoomType.PUZZLE -> Puzzle.fromName(room.data.name)?.let { Dungeon.Info.puzzles.putIfAbsent(it, false) }
            else -> {}
        }
    }

    fun addTile(x: Int, y: Int, tile: Room) {
        tiles.removeIf { it.x == tile.x && it.z == tile.z }
        tiles.add(tile)

        if (x < topLeft.first || (x == topLeft.first && y < topLeft.second)) {
            topLeft = Pair(x, y)
            mainRoom = tile
        }

        if (tiles.size == 1) {
            center = Pair(x, y)
            return
        }

        val positions = tiles.mapNotNull {
            it.getArrayPosition().takeIf { (arrX, arrZ) ->
                arrX % 2 == 0 && arrZ % 2 == 0
            }
        }

        if (positions.isEmpty()) return

        val xRooms = positions.groupBy { it.first }.entries.sortedByDescending { it.value.size }
        val zRooms = positions.groupBy { it.second }.entries.sortedByDescending { it.value.size }

        center = when {
            zRooms.size == 1 || zRooms[0].value.size != zRooms[1].value.size -> {
                xRooms.sumOf { it.key } / xRooms.size to zRooms[0].key
            }

            xRooms.size == 1 || xRooms[0].value.size != xRooms[1].value.size -> {
                xRooms[0].key to zRooms.sumOf { it.key } / zRooms.size
            }

            else -> (xRooms[0].key + xRooms[1].key) / 2 to (zRooms[0].key + zRooms[1].key) / 2
        }
    }

    fun getCheckmarkPosition() = if (DungeonMapConfig.centerStyle) center else topLeft
}
