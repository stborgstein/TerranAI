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
			boolean refinery = false;
			
			
						//Should get a worker to make a refinery at the vespine geysir HOWEVER it doesn't
			if (myUnit.getType().isWorker() && myUnit.isGatheringMinerals() && !refinery) {
				Unit closestGas = null;
				for (Unit neutralGas : game.neutral().getUnits()) {
					if (neutralGas.getType().isResourceContainer() && !neutralGas.getType().isMineralField()) {
						if(self.minerals() >= 100 && self.supplyUsed() == 12)
						{
							buildingTrain(myUnit, UnitType.Terran_Refinery, neutralGas.getTilePosition());
							if(!myUnit.canBuild(UnitType.Terran_Refinery, neutralGas.getTilePosition()))
								System.out.println("Can't Refinery Build there");
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
				
			
						//Continues training SCV's until 20 is created
			if (self.minerals() >= 50 && self.supplyUsed() < 30 && self.supplyUsed() != self.supplyTotal()) {
				for(int i = 0; i <= 20; i++) {
					unitTrain(UnitType.Terran_SCV, UnitType.Terran_Command_Center);
				}

			}
		
			
						//Continues training marines from Barracks while supply between 20 and 100
			if(self.minerals() >= 50 && 20 <= self.supplyUsed() && self.supplyUsed() <= 100 && self.supplyUsed() != self.supplyTotal()) {
				unitTrain(UnitType.Terran_Marine, UnitType.Terran_Barracks);
			}
			
			
						//Should build a barracks at CommandCenter x + 12 and y +10 TilePosition HOWEVER it doesn't
			if(self.minerals() >= 100 && self.supplyUsed() == 20){
				if(myUnit.getType().isWorker() && myUnit.isGatheringMinerals()) {
					for(Unit commandCenter : self.getUnits()) {
						if(commandCenter.getType() == UnitType.Terran_Command_Center) {
							TilePosition building = new TilePosition(commandCenter.getX() + 12, commandCenter.getY() + 10);
							
							if(myUnit.canBuild(UnitType.Terran_Barracks, myUnit.getTilePosition())){
								buildingTrain(myUnit, UnitType.Terran_Barracks, building);
								if(!myUnit.canBuild(UnitType.Terran_Barracks, commandCenter.getTilePosition()))
									System.out.println("Can't Barracks Build there");
							}
						}
					}
				}
			}
			

						//Should build a Supply Depot at CommandCenter x + 8 and y + 6 TilePosition HOWEVER it doesn't
			if(self.supplyUsed() == self.supplyTotal() && self.minerals() >= 100){
				if(myUnit.getType().isWorker() && myUnit.isGatheringMinerals()){
					for(Unit commandCenter : self.getUnits()) {
						if(commandCenter.getType() == UnitType.Terran_Command_Center) {
							TilePosition building = new TilePosition(commandCenter.getX() + 8, commandCenter.getY() + 6);
								
							if(myUnit.canBuild(UnitType.Terran_Supply_Depot, myUnit.getTilePosition())){
								buildingTrain(myUnit, UnitType.Terran_Supply_Depot, building);
								if(!myUnit.canBuild(UnitType.Terran_Supply_Depot, commandCenter.getTilePosition()))
									System.out.println("Can't Supply Depot Build there");
							}		
						}		
					}
				}
			}
			
			
						//Locating in which direction the minerals are compared to the Command Center
						// and from there initiates building in the opposite side
			/*if(myUnit.getType() == UnitType.Terran_Command_Center) {
				
				myUnit.getX();
				myUnit.getY();
				
				for (Unit myUnit2 : self.getUnits()) {
					if (myUnit2.getType().isMineralField()) {
						//initial_minimal_distance = 0;
						for (int i = 0; i <8; i++) {
							//int minimal_distance = 100000;
							if(myUnit2.getDistance(myUnit) > myUnit2.getDistance(myUnit.getX() + 1, myUnit.getY())) {
								//minimal_distance = 
							}
							else if(myUnit2.getDistance(myUnit) > myUnit2.getDistance(myUnit.getX() - 1, myUnit.getY())) {
								
							}
							else if(myUnit2.getDistance(myUnit) > myUnit2.getDistance(myUnit.getX(), myUnit.getY() + 1)) {
								
							}
							else if(myUnit2.getDistance(myUnit) > myUnit2.getDistance(myUnit.getX(), myUnit.getY() - 1)) {
								
							}
						}
						
						
					}
				}
			}*/
		}
	}

	
	/*Takes two different UnitType parameters and sends them into a general function
	 and selects a spesific building and creates a certain unit from said building */
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
