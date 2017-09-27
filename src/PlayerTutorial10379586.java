/* Sara Yuste Fernandez Alonso, NIA 100330038
 * Stefan Borgstein, NIA 100379586*/
import java.util.ArrayList;
import java.util.List;

import bwapi.*;
import bwta.BWTA;

public class PlayerTutorial10379586 extends DefaultBWListener {
	private Mirror mirror = new Mirror();

	private Game game;

	private Player self;

	public boolean refinery = false;
	public boolean barrack = false;

	public List<Unit> IdleWorkers = new ArrayList<Unit>();
	public List<Unit> BusyWorkersMineralsOrGas = new ArrayList<Unit>();
	//List<Unit> BusyWorkersGas = new ArrayList<Unit>();
	public List<Unit> BusyWorkersSupply = new ArrayList<Unit>();

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

	}

	public void onFrame() {

		// game.setTextSize(10); Podemos modificar parametros de la interfaz del juego
		game.drawTextScreen(10, 10, "Jugando como " + self.getName() + " - " + self.getRace());
		// Get all the workers to either gather minerals or gas
		if (!IdleWorkers.isEmpty()) {
			// Get the first idle worker
			Unit myUnit = IdleWorkers.remove(0);
			if (self.minerals() >= 100)
				buildRefinery(myUnit);
			else if (self.minerals() < 100) {
				gatherMinerals(myUnit);
			}
		}

		// Train SCVs if the total number of workers is less than 10
		if (IdleWorkers.size() + BusyWorkersMineralsOrGas.size() <= 10) {
			unitTrain(UnitType.Terran_SCV, UnitType.Terran_Command_Center);
		} else if(BusyWorkersMineralsOrGas.size() == 10 && self.minerals() >= 150) {
			buildBarrack();
			barrack = true;
		}

		// Trains marines if there is a barrack
		if (barrack) {
			unitTrain(UnitType.Terran_Marine, UnitType.Terran_Barracks);

		}

		else if (self.supplyUsed() == self.supplyTotal() && self.minerals() >= 100) {
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
		if (myUnit.canBuild(UnitType.Terran_Supply_Depot, building)) {
			buildingTrain(myUnit, UnitType.Terran_Supply_Depot, building);
			if (!myUnit.canBuild(UnitType.Terran_Supply_Depot, building))
				System.out.println("Can't Build Supply Depot there");
		}

	}

	public void buildBarrack() {
		Unit myUnit = BusyWorkersMineralsOrGas.remove(0);

		TilePosition building = findLocationToBuild();
		if (!myUnit.canBuild(UnitType.Terran_Barracks, building)) {
			System.out.println("Can't Build Barracks there");
		} else {
			buildingTrain(myUnit, UnitType.Terran_Barracks, building);
			System.out.println("Barracks built");

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
			BusyWorkersMineralsOrGas.add(myUnit);
		}
	}

	public TilePosition findLocationToBuild() {
		// Locating in which direction the minerals are compared to the Command Center
		// and from there initiates building in the opposite side
		// Opposite side 4 tiles away from command Center
		int base_x = 0;
		int base_y = 0;
		TilePosition BuildingLocation = new TilePosition(base_x, base_y);

		for (Unit myUnit : self.getUnits()) {
			if (myUnit.getType() == UnitType.Terran_Command_Center) {

				int command_center_x = myUnit.getX();
				int command_center_y = myUnit.getY();

				for (Unit myUnit2 : self.getUnits()) {
					if (myUnit2.getType().isMineralField()) {
						int mineral_x = myUnit2.getX();
						int mineral_y = myUnit2.getY();
						if (mineral_x < command_center_x && mineral_y == command_center_y) {
							// located to the east
							base_x = command_center_x + 2;
							base_y = command_center_y;
						} else if (mineral_x > command_center_x && mineral_y == command_center_y) {
							// located to the west
							base_x = command_center_x - 2;
							base_y = command_center_y;
						} else if (mineral_x == command_center_x && mineral_y < command_center_y) {
							// located to the north
							base_x = command_center_x;
							base_y = command_center_y + 2;
						} else if (mineral_x == command_center_x && mineral_y > command_center_y) {
							// located to the south
							base_x = command_center_x;
							base_y = command_center_y - 2;
						} else if (mineral_x < command_center_x && mineral_y < command_center_y) {
							// located to the north east
							base_x = command_center_x;
							base_y = command_center_y + 2;
						} else if (mineral_x > command_center_x && mineral_y < command_center_y) {
							// located to the north west
							base_x = command_center_x;
							base_y = command_center_y + 2;
						} else if (mineral_x > command_center_x && mineral_y > command_center_y) {
							// located to the south west
							base_x = command_center_x;
							base_y = command_center_y - 2;
						} else if (mineral_x < command_center_x && mineral_y > command_center_y) {
							// located to the south east
							base_x = command_center_x;
							base_y = command_center_y - 2;
						}
					}
				}
				BuildingLocation = new TilePosition(base_x, base_y);
			}
		}
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

	public void onUnitComplete(Unit arg0) {
		Unit u = arg0;
		for (Unit myUnit : self.getUnits()) {
			if (u.getID() == myUnit.getID()) {
				// the unit is self
				if (u.getType().isWorker())
					IdleWorkers.add(u);
				System.out.println("\\UNIDAD COMPLETADA: " + u.getType());
			}
		}
	}

	public static void main(String[] args) {
		new PlayerTutorial10379586().run();
	}
}
