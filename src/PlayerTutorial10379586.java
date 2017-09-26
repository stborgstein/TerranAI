import bwapi.*;
import bwta.BWTA;

public class PlayerTutorial10379586 extends DefaultBWListener {
	private Mirror mirror = new Mirror();

	private Game game;

	private Player self;

	public boolean refinery = false;

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

		// int i = 0;
		// for(BaseLocation baseLocation : BWTA.getBaseLocations()){
		// System.out.println("Base location #" + (++i) + ". Printing location's region
		// polygon:");
		// for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
		// System.out.print(position + ", ");
		// }
		// System.out.println();
		// }

	}

	public void onFrame() {


		//game.setTextSize(10); Podemos modificar parametros de la interfaz del juego
		game.drawTextScreen(10, 10, "Jugando como " + self.getName() + " - " + self.getRace());

		StringBuilder units = new StringBuilder("Mis unidades:\n");
		
		//Esta es una forma de iterar sobre las unidades, aunque lo mejor es crear listas propias y manejarlas << importante
		for (Unit myUnit : self.getUnits()) {
			units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
			
			System.out.println("Supply used 1: " + self.supplyUsed());
			
						//Should get a worker to make a refinery at the vespine geysir HOWEVER it doesn't
			if (myUnit.getType().isWorker() && myUnit.isGatheringMinerals() && !refinery) {
				Unit closestGas = null;
				for (Unit neutralGas : game.neutral().getUnits()) {
					if (neutralGas.getType().isResourceContainer() && !neutralGas.getType().isMineralField()) {
						if(self.minerals() >= 100) // && self.supplyUsed() == 12)
						{
							buildingTrain(myUnit, UnitType.Terran_Refinery, neutralGas.getTilePosition());
							if(!myUnit.canBuild(UnitType.Terran_Refinery, neutralGas.getTilePosition()))
								System.out.println("Can't Refinery Build there");
							else {
								refinery = true;
							}
							
						}

						if (closestGas == null || myUnit.getDistance(neutralGas) < myUnit.getDistance(closestGas)) {
							closestGas = neutralGas;
						}
					}
				}
				if (closestGas != null) {
					myUnit.gather(closestGas, false);
				}
			}
			
			
						//Gets every idle worker to get back to working on mining minerals
			if (myUnit.getType().isWorker() && myUnit.isIdle()) {
				Unit closestMineral = null;

				// busca la mina mas cercana. Las minas de mineral y vespeni asi como la
				// refineria son neutral units
				for (Unit neutralUnit : game.neutral().getUnits()) {
					if (neutralUnit.getType().isMineralField()) {
						if (closestMineral == null
							|| myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
							closestMineral = neutralUnit;
						}
					}
				}

				// si se encuentra la mina, mandar al trabajador alli
				if (closestMineral != null) {
					myUnit.gather(closestMineral, false);
				}
			}
				
			System.out.println("Supply used 2: " + self.supplyUsed());
			
						//Continues training SCV's until 20 is created
			if (self.minerals() >= 50 && self.supplyUsed() < 30 && self.supplyUsed() != self.supplyTotal()) {
				for(int i = 0; i <= 20; i++) {
					unitTrain(UnitType.Terran_SCV, UnitType.Terran_Command_Center);
				}

			}
			System.out.println("Supply used 3: " + self.supplyUsed());
		
			
						//Continues training marines from Barracks while supply between 20 and 100
			if(self.minerals() >= 50 && 20 <= self.supplyUsed() && self.supplyUsed() <= 100 && self.supplyUsed() != self.supplyTotal()) {
				unitTrain(UnitType.Terran_Marine, UnitType.Terran_Barracks);
			}
			
			
						//Should build a barracks at CommandCenter x + 12 and y +10 TilePosition HOWEVER it doesn't
			if(self.minerals() >= 100 && self.supplyUsed() == 20){
				if(myUnit.getType().isWorker() && myUnit.isGatheringMinerals()) {
					for(Unit commandCenter : self.getUnits()) {
						if(commandCenter.getType() == UnitType.Terran_Command_Center) {
							TilePosition building = new TilePosition(commandCenter.getX() - 1, commandCenter.getY() + 1);
							
							if(myUnit.canBuild(UnitType.Terran_Barracks, building)){
								buildingTrain(myUnit, UnitType.Terran_Barracks, building);
								System.out.println("Barracks built");
								if(!myUnit.canBuild(UnitType.Terran_Barracks, building))
									System.out.println("Can't Barracks Build there");
							}
						}
					}
				}
			}
			
			System.out.println("Supply used 4: " + self.supplyUsed() + " supplyTotal: "+ self.supplyTotal() );

						//Should build a Supply Depot at CommandCenter x + 8 and y + 6 TilePosition HOWEVER it doesn't
			if(self.supplyUsed() == self.supplyTotal() && self.minerals() >= 100){
				if(myUnit.getType().isWorker() && myUnit.isGatheringMinerals()){
					for(Unit commandCenter : self.getUnits()) {
						if(commandCenter.getType() == UnitType.Terran_Command_Center) {
							TilePosition building = new TilePosition(commandCenter.getX() - 2, commandCenter.getY() + 2);
								
							if(myUnit.canBuild(UnitType.Terran_Supply_Depot, building)){
								buildingTrain(myUnit, UnitType.Terran_Supply_Depot, building);
								if(!myUnit.canBuild(UnitType.Terran_Supply_Depot, building))
									System.out.println("Can't Supply Depot Build there");
							}		
						}		
					}
				}
			}
			
			
						//Locating in which direction the minerals are compared to the Command Center
						// and from there initiates building in the opposite side
			//Opposite side 4 tiles away from command Center
			if(myUnit.getType() == UnitType.Terran_Command_Center) {
				
				int command_center_x = myUnit.getX();
				int command_center_y = myUnit.getY();
				
				int base_x;
				int base_y;
				
				for (Unit myUnit2 : self.getUnits()) {
					if (myUnit2.getType().isMineralField()) {
						int mineral_x = myUnit2.getX();
						int mineral_y = myUnit2.getY();
						if (mineral_x < command_center_x && mineral_y == command_center_y) {
							//located to the east
							base_x = command_center_x + 4;
							base_y = command_center_y;
						}
						else if (mineral_x > command_center_x && mineral_y == command_center_y) {
							//located to the west
							base_x = command_center_x - 4;
							base_y = command_center_y;
						}
						else if (mineral_x == command_center_x && mineral_y < command_center_y) {
							//located to the north
							base_x = command_center_x;
							base_y = command_center_y + 4;
						}
						else if (mineral_x == command_center_x && mineral_y > command_center_y) {
							//located to the south
							base_x = command_center_x;
							base_y = command_center_y - 4;
						}
						else if (mineral_x < command_center_x && mineral_y < command_center_y) {
							//located to the north east
							base_x = command_center_x;
							base_y = command_center_y + 4;
						}
						else if (mineral_x > command_center_x && mineral_y < command_center_y) {
							//located to the north west
							base_x = command_center_x;
							base_y = command_center_y + 4;
						}
						else if (mineral_x > command_center_x && mineral_y > command_center_y) {
							//located to the south west
							base_x = command_center_x;
							base_y = command_center_y - 4;
						}
						else if (mineral_x < command_center_x && mineral_y > command_center_y) {
							//located to the south east
							base_x = command_center_x;
							base_y = command_center_y - 4;
						}
					}
				}
			}
		}
	}

	
	private void CASE(boolean b) {
		// TODO Auto-generated method stub
		
	}

	/*Takes two different UnitType parameters and sends them into a general function
	 and selects a specific building and creates a certain unit from said building */
	public void unitTrain(UnitType unit, UnitType building) {
		
		
		if(unit == UnitType.Terran_Marine && building == UnitType.Terran_Barracks)
		{
			for (Unit myUnit : self.getUnits())
				if (myUnit.getType()==UnitType.Terran_Barracks)
					myUnit.train(unit);

		}
		if(unit == UnitType.Terran_SCV && building == UnitType.Terran_Command_Center)
		{
			for (Unit myUnit : self.getUnits()) {
				if (myUnit.getType()==UnitType.Terran_Command_Center) {
					myUnit.train(unit);
				}
			}
		}
	}
	
	
	/*Takes three parameters, Unit builder, UnitType building, TilePosition position. From these
	 * three parameters it gets a unit which is to build a building (UnitType) on a set
	  location (TilePosition)																	*/
	public void buildingTrain(Unit builder, UnitType building, TilePosition position) {		
		builder.build(building, position);		
	}
	
	
	public void onUnitComplete(Unit arg0) {
		Unit u = arg0;     
		System.out.println("\\UNIDAD COMPLETADA: "+u.getType());
	}
	
	
	public static void main(String[] args) {
		new PlayerTutorial10379586().run();
	}
}
