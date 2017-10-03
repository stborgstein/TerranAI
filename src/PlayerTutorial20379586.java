
/* Sara Yuste Fernandez Alonso, NIA 100330038
 * Stefan Borgstein, NIA 100379586*/

import java.util.ArrayList;
import java.util.List;

import bwapi.*;
import bwta.BWTA;

public class PlayerTutorial20379586 extends DefaultBWListener {
	private Mirror mirror = new Mirror();

	private Game game;

	private Player self;

	public boolean refinery = false;
	public boolean barrack = false;

	public List<Unit> IdleWorkers = new ArrayList<Unit>();
	public List<Unit> BusyWorkersMineralsOrGas = new ArrayList<Unit>();
	public List<Unit> BusyWorkersMineralsOrGas2 = new ArrayList<Unit>();
	public List<Unit> BusyWorkersSupply = new ArrayList<Unit>();
	public List<Unit> Marines = new ArrayList<Unit>();

	public String[][] mapMatrix;

	public int i = 0;

	public void run() {
		mirror.getModule().setEventListener(this);
		mirror.startGame();
	}

	public void onStart() {

		game = mirror.getGame();
		self = game.self();

		game.enableFlag(1);
		game.setLocalSpeed(10);

		// La clase BWTA es la que contiene informacion y llamadas al mapa
		// La primera vez que se ejecuta el bot sobre un mapa tarda siempre unos minutos
		// en procesarlo, es normal
		System.out.println("Analyzing map...");
		BWTA.readMap();
		BWTA.analyze();

		System.out.println("Map data ready");

		generateMapMatrix();

	}

	public void onFrame() {

		// game.setTextSize(10); Podemos modificar parametros de la interfaz del juego
		game.drawTextScreen(10, 10, "Jugando como " + self.getName() + " - " + self.getRace());
		// Get all the workers to either gather minerals or gas
		if (!IdleWorkers.isEmpty()) {
			// Get the first idle worker
			Unit myUnit = IdleWorkers.remove(0);
			if (game.canMake(UnitType.Terran_Refinery, myUnit) && !refinery && BusyWorkersMineralsOrGas.size() == 14)
				buildRefinery(myUnit);
			else {
				gatherMinerals(myUnit);
			}
		}

		// Train SCVs if the total number of workers is less than 10
		if (self.supplyUsed() <= 22) {
			unitTrain(UnitType.Terran_SCV, UnitType.Terran_Command_Center);

		} else if (BusyWorkersMineralsOrGas2.size() >= 12 && game.canMake(UnitType.Terran_Barracks)
				&& BusyWorkersSupply.size() < 1) {
			Unit myUnit = BusyWorkersMineralsOrGas.remove(0);

			buildBarrack(myUnit);
			barrack = true;
		}

		// Trains marines if there is a barrack
		if (barrack) {
			unitTrain(UnitType.Terran_Marine, UnitType.Terran_Barracks);

		}

		if (self.supplyUsed() == self.supplyTotal() && game.canMake(UnitType.Terran_Supply_Depot)
				&& BusyWorkersSupply.size() < 1) {
			if (IdleWorkers.isEmpty()) {
				Unit myUnit2 = BusyWorkersMineralsOrGas.remove(0);
				buildSupplyDepot(myUnit2);
			} else {
				Unit myUnit2 = IdleWorkers.remove(0);
				buildSupplyDepot(myUnit2);
			}
		}

	}

	public void buildSupplyDepot(Unit myUnit) {
		BusyWorkersSupply.add(myUnit);
		TilePosition building = findLocationToBuild();
		int depo_x = building.getX() / 32;
		int depo_y = building.getY() / 32;
		TilePosition depoLocal = new TilePosition(depo_x, depo_y);
		buildingTrain(myUnit, UnitType.Terran_Supply_Depot, depoLocal);

	}

	public void buildBarrack(Unit myUnit) {

		TilePosition building = findLocationToBuild();
		int barr_x = (building.getX() / 32) - 4;
		int barr_y = building.getY() / 32;

		TilePosition BarrLocal = new TilePosition(barr_x, barr_y);

		if (!myUnit.canBuild(UnitType.Terran_Barracks, BarrLocal)) {
			System.out.println("Can't Build Barracks there");
		} else {
			buildingTrain(myUnit, UnitType.Terran_Barracks, BarrLocal);
		}
	}

	public void buildRefinery(Unit myUnit) {
		Unit closestGas = null;
		for (Unit neutralGas : game.neutral().getUnits()) {
			if (neutralGas.getType().isResourceContainer() && !neutralGas.getType().isMineralField()) {

				if (!myUnit.canBuild(UnitType.Terran_Refinery, neutralGas.getTilePosition()))
					System.out.println("Can't Build Refinery there");
				else {
					buildingTrain(myUnit, UnitType.Terran_Refinery, neutralGas.getTilePosition());
					refinery = true;
				}

				if (closestGas == null || myUnit.getDistance(neutralGas) < myUnit.getDistance(closestGas)) {
					closestGas = neutralGas;
				}
			}
		}
		if (closestGas != null) {
			myUnit.gather(closestGas, false);
		}
		BusyWorkersMineralsOrGas.add(myUnit);
	}

	public void gatherMinerals(Unit myUnit) {
		// Gets workers back to working on mining minerals
		Unit closestMineral = null;

		// Find the closest Mineral Field to mine
		for (Unit neutralUnit : game.neutral().getUnits()) {
			if (neutralUnit.getType().isMineralField()) {
				if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
					closestMineral = neutralUnit;
				}
			}
		}
		// Send the worker to mine the field
		if (closestMineral != null) {
			myUnit.gather(closestMineral, false);

			if (BusyWorkersMineralsOrGas.size() == 12) {
				BusyWorkersMineralsOrGas2.add(myUnit);
			} else {
				BusyWorkersMineralsOrGas.add(myUnit);
			}
		}
	}

	public TilePosition findLocationToBuild() {
		// Locating in which direction the minerals are compared to the Command Center
		// and from there initiates building in the opposite side
		// Opposite side 4 tiles away from Command Center
		int base_x = 0;
		int base_y = 0;

		for (Unit myUnit : self.getUnits()) {
			if (myUnit.getType() == UnitType.Terran_Command_Center) {

				int command_center_x = myUnit.getX();
				int command_center_y = myUnit.getY();

				for (Unit myUnit2 : game.neutral().getUnits()) {
					if (myUnit2.getType().isMineralField()) {
						int mineral_x = myUnit2.getX();
						int mineral_y = myUnit2.getY();
						if (mineral_x < command_center_x && mineral_y == command_center_y) {
							// located to the east
							base_x = command_center_x + 128;
							base_y = command_center_y;
						} else if (mineral_x > command_center_x && mineral_y == command_center_y) {
							// located to the west
							base_x = command_center_x - 128;
							base_y = command_center_y;
						} else if (mineral_x == command_center_x && mineral_y < command_center_y) {
							// located to the north
							base_x = command_center_x;
							base_y = command_center_y + 128;
						} else if (mineral_x == command_center_x && mineral_y > command_center_y) {
							// located to the south
							base_x = command_center_x;
							base_y = command_center_y - 128;
						} else if (mineral_x < command_center_x && mineral_y < command_center_y) {
							// located to the north east
							base_x = command_center_x;
							base_y = command_center_y + 128;
						} else if (mineral_x > command_center_x && mineral_y < command_center_y) {
							// located to the north west
							base_x = command_center_x;
							base_y = command_center_y + 128;
						} else if (mineral_x > command_center_x && mineral_y > command_center_y) {
							// located to the south west
							base_x = command_center_x;
							base_y = command_center_y - 128;
						} else if (mineral_x < command_center_x && mineral_y > command_center_y) {
							// located to the south east
							base_x = command_center_x;
							base_y = command_center_y - 128;
						}
					}
				}
			}
		}

		TilePosition BuildingLocation = new TilePosition(base_x, base_y);
		return BuildingLocation;
	}

	/*
	 * Takes two different UnitType parameters and sends them into a general
	 * function and selects a specific building and creates a certain unit from said
	 * building
	 */
	public void unitTrain(UnitType unit, UnitType building) {

		if (unit == UnitType.Terran_Marine && building == UnitType.Terran_Barracks) {
			for (Unit myUnit : self.getUnits())
				if (myUnit.getType() == UnitType.Terran_Barracks)
					myUnit.train(unit);

		}
		if (unit == UnitType.Terran_SCV && building == UnitType.Terran_Command_Center) {
			for (Unit myUnit : self.getUnits()) {
				if (myUnit.getType() == UnitType.Terran_Command_Center) {
					myUnit.train(unit);
				}
			}
		}
	}

	/*
	 * Takes three parameters, Unit builder, UnitType building, TilePosition
	 * position. From these three parameters it gets a unit which is to build a
	 * building (UnitType) on a set location (TilePosition)
	 */
	public void buildingTrain(Unit builder, UnitType building, TilePosition position) {
		builder.build(building, position);
	}

	public void generateMapMatrix() {
		
		mapMatrix = new String[game.mapHeight()][game.mapWidth()];
		
		for (int i = 0; i < game.mapHeight(); i++) {
			int tileY = i;
			for (int j = 0; j < game.mapWidth(); j++) {
				int tileX = j;
				TilePosition checkTile = new TilePosition(tileX, tileY);
				// Check if the position is not buildable, including already existing buildings in that position
				if (!game.isBuildable(checkTile, true)) {
					mapMatrix[tileX][tileY] = "0";
				
				} else {				
					// Check if it's a Vespene Geyser or Mineral Field
					for (Unit neutralUnit : game.neutral().getUnits()) {
						if (neutralUnit.getX() / 32 == tileX && neutralUnit.getY() / 32 == tileY) {
							if (neutralUnit.getType().isMineralField()) {
								mapMatrix[tileX][tileY] = "M";
							} else {
								mapMatrix[tileX][tileY] = "V";
							}
						
						} else {
							// If the position is buildable, check how many tiles available to build for that position
							int right = tileX + 1;
							int down = tileY + 1;
							int maximum = 1;
							TilePosition explore = new TilePosition(right, down);

							while (game.isBuildable(explore, true) && !(mapMatrix[explore.getX()][explore.getY()] == "M") 
									&& !(mapMatrix[explore.getX()][explore.getY()] == "V")) {
								
								if(right < game.mapWidth()) {
									right++;
								} else
									break;
								
								if(down < game.mapHeight()) {
									down++;
								} else
									break;
								
								explore = new TilePosition(right, down);

								
								maximum++;								
								
								if (maximum == 4)
									break;
							} 
							mapMatrix[tileX][tileY] = Integer.toString(maximum);
						}
					}

				}

			}
		}

		for (int i = 0; i < game.mapHeight();) {
			for (int j = 0; j < game.mapWidth(); j++) {
				System.out.print(mapMatrix[j][i] + " - ");
				
				if(j == game.mapWidth() - 1) {
					System.out.println(' ');
					i++;
					j = 0;
				}
			}
		}
	}
	
	
	public TilePosition matrixBuild(UnitType building) {

		int x = 0, y = 0;
		int max = 1;
		
		for(Unit myUnit : self.getUnits()) {
			if(myUnit.getType() == UnitType.Terran_Command_Center) {		
				x = myUnit.getX();
				y = myUnit.getY();
				
				TilePosition z = building.tileSize();
				
				String zX = "" + z.getX();
				
				while(mapMatrix[x][y] != zX) {
					for(int a = 0; a <= max; a++) {
						if(0 <= x && x <= game.mapWidth())
							x--;
					}
					for(int b = 0;  b <= max; b++) {
						if(0 <= y && y <= game.mapHeight())
							y--;
					}
					max++;
					for(int c = 0; c <= max; c++) {
						if(0 <= x && x <= game.mapWidth())
						x++;
					}
					for(int d = 0; d <= max; d++) {
						if(0 <= y && y <= game.mapHeight())
							y++;
					}
					max++;
				}								
				
			}
		}
		TilePosition Tile = new TilePosition(x, y);
		return Tile;
	}
	//Updating matrix using double TilePosition
public void updateMatrixDoblePos(TilePosition startTile, TilePosition endTile) {
		
		int sTileX = startTile.getX();
		int sTileY = startTile.getY();
		
		int eTileX = endTile.getX();
		int eTileY = endTile.getY();
		
		int zX = sTileX - eTileX;
		int zY = sTileY - eTileY;
		
		for(int i = 0; i <= zY; i++) {
			for(int j = 0; j <= zX; j++) {
				mapMatrix[sTileX][sTileY] = "0";
				sTileX++;
			}
			sTileY++;
		}	
	}
	
		//Updating matrix using building type and its top left corner
	public void updateMatrix(UnitType building, TilePosition tile) {
		
		TilePosition z = building.tileSize();
		
		int zX = z.getX();
		int zY = z.getY();
		
		int tileX = tile.getX();
		int tileY = tile.getY();
		
		for(int i = 0; i <= zY; i++) {
			for(int j = 0; j <= zX; j++) {
				mapMatrix[tileX][tileY] = "0";
				tileX++;
			}
			tileY++;
		}

	}

	public void onUnitComplete(Unit arg0) {
		Unit u = arg0;

		for (Unit myUnit : self.getUnits()) {
			if (u.getID() == myUnit.getID()) {
				// the unit is self
				if (u.getType().isWorker())
					IdleWorkers.add(u);
				System.out.println("UNIDAD COMPLETADA: " + u.getType());
			}

			if(u.getID() == myUnit.getID()) {
				if(u.getType() == UnitType.Terran_Command_Center) {
					System.out.println("UNIDAD COMPLETADA: " + u.getType());
					updateMatrix(u.getType(), u.getTilePosition());
					System.out.println(u.getTilePosition());
				}
			}
			if (u.getID() == myUnit.getID()) {
				if (u.getType() == UnitType.Terran_Supply_Depot) {
					System.out.println("UNIDAD COMPLETADA: " + u.getType());
					Unit unitBuilder = BusyWorkersSupply.remove(0);
					gatherMinerals(unitBuilder);
					updateMatrix(u.getType(), u.getTilePosition());
					
				}
			}
			if (u.getID() == myUnit.getID()) {
				if (u.getType() == UnitType.Terran_Barracks) {
					System.out.println("UNIDAD COMPLETADA: " + u.getType());
					Unit unitBuilder = BusyWorkersSupply.remove(0);
					IdleWorkers.add(unitBuilder);
					updateMatrix(u.getType(), u.getTilePosition());
					
				}
			}

			if (u.getID() == myUnit.getID()) {
				if (u.getType() == UnitType.Terran_Marine) {
					Marines.add(u);
				}
			}
		}
	}

	public static void main(String[] args) {
		new PlayerTutorial20379586().run();
	}
}
