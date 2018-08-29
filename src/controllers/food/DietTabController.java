package controllers.food;

import java.io.IOException;
/* Import java, javafx, mainPackage */
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

import controllers.MainProgramController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Day;
import model.Food;
import model.Helper;

public class DietTabController extends BaseFoodController implements Initializable {

	@FXML
	private TableView<Food> tvEntries;

	@FXML
	private TableColumn<Food, String> tcFoods, tcAmount, tcCalories, tcCarbs, tcFats, tcProts,
			tcQuantity;

	@FXML
	private Button btnAddEntry, btnEdit, btnDelete, btnCustom;

	@FXML
	private TextField tfCalories, tfCarbs, tfFats, tfProtein;

	@FXML
	private DatePicker dpDate;

	// Used to check the current loaded date and day
	private static LocalDate currentDate;
	private static Day currentDay;

	public void initialize(URL arg0, ResourceBundle arg1) {

		// Setup the date to today and in turn only load that days data
		setupDay();
		setupTable();
		setupPieChart();
		setupDatePicker();

		// Load the foods into an arraylist
		loadAddedFoods();
		// Loads the foods into the GUI table
		loadTableFoods();
		// Update the values of total calories, carbs, fats, protein etc
		updateTotalValues();
		// Update the GUI
		update();
	}

	private void setupDay() {
		dpDate.setValue(LocalDate.now());
		currentDate = dpDate.getValue();
		currentDay = MainProgramController.getDay(currentDate);
	}

	private void setupTable() {
		// Initialize the person table with the two columns.
		tcFoods.setCellValueFactory(cellData -> cellData.getValue().getStrFood());
		tcAmount.setCellValueFactory(cellData -> cellData.getValue().getStrAmount());
		tcCalories.setCellValueFactory(cellData -> cellData.getValue().getStrCalories());
		tcCarbs.setCellValueFactory(cellData -> cellData.getValue().getStrCarbs());
		tcFats.setCellValueFactory(cellData -> cellData.getValue().getStrFats());
		tcProts.setCellValueFactory(cellData -> cellData.getValue().getStrProts());
		tcQuantity.setCellValueFactory(cellData -> cellData.getValue().getStrQuantity());

		// Add observable list data to the table
		tvEntries.setItems(foodData);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setupDatePicker() {
		dpDate.setOnAction(new EventHandler() {
			public void handle(Event t) {
				LocalDate date = dpDate.getValue();

				// Update the currentDay
				currentDay = MainProgramController.getDay(date);

				// Clear the table
				addedFoods.clear();
				foodData.clear();

				// Load the food into the table
				loadAddedFoods();
				loadTableFoods();
				updateTotalValues();
				update();
			}
		});
	}

	private void loadAddedFoods() {
		// loop through the currentDay food
		for (int i = 0; i < currentDay.getFoods().size(); i++) {
			Food f = currentDay.getFoods().get(i);
			addedFoods.add(f);
		}
	}


	/**
	 * loads the local memory arraylist into the GUI table arraylist
	 */
	private void loadTableFoods() {
		// Calculate total data
		for (int i = 0; i < addedFoods.size(); i++) {
			Food f = addedFoods.get(i);
			foodData.add(f);
		}
	}

	private void update() {
		updatePieChart();
		updateGUIMacrosInfo();
		tvEntries.refresh();
	}


	private void updateGUIMacrosInfo() {
		tfCalories.setText(Double.toString(Helper.round(calories, 2)));
		tfCarbs.setText(Double.toString(Helper.round(carbs, 2)));
		tfFats.setText(Double.toString(Helper.round(fats, 2)));
		tfProtein.setText(Double.toString(Helper.round(protein, 2)));
	}

	@FXML
	protected void handleEdit(ActionEvent event) throws IOException {
		System.out.println("Create edit food window here");

		try {
			// Get the current selected food
			Food selectedFood = tvEntries.getSelectionModel().getSelectedItem();

			// Check if this is a custom food
			if (selectedFood.getCustom()) {
				handleCustomEdit(selectedFood);
			} else {
				handleNormalEdit(selectedFood);
			}

			// Refresh the table
			update();

		} catch (Exception e) {
			System.out.println("Couldn't create edit window..?");
		}
	}

	private void handleCustomEdit(Food selectedFood) throws IOException {
		// Create window
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(getClass().getResource("/view/dietTabEditCustomFoodWindow.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 355, 270);
		Stage stage = new Stage();
		Stage parent = (Stage) btnEdit.getScene().getWindow();
		stage.initOwner(parent);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Edit Food");
		stage.setScene(scene);
		// Controller access
		EditCustomFoodController controller = fxmlLoader.<EditCustomFoodController>getController();
		controller.setSpinnerValue(Double.toString(selectedFood.getQuantity()));

		// pass food values into textfield
		String[] values = new String[] {
				// Remove (custom)
				selectedFood.getName(), Double.toString(selectedFood.getAmount()),
				Double.toString(selectedFood.getCarbohydrates()), Double.toString(selectedFood.getFats()),
				Double.toString(selectedFood.getProteins()) };

		controller.setTextFieldValues(values);
		// showAndWait will block execution until the window closes...
		stage.showAndWait();

		// get the updated values and set them into the selected food
		String[] retVals = controller.getValues();

		// Get values back from controller and update them into the food object
		selectedFood.setName(retVals[0]);
		selectedFood.setAmount(Double.parseDouble(retVals[1]));
		selectedFood.setCarbohydrates(Double.parseDouble(retVals[2]));
		selectedFood.setFats(Double.parseDouble(retVals[3]));
		selectedFood.setProteins(Double.parseDouble(retVals[4]));

		// Quantity last value that gets updated because we need new values above
		selectedFood.setQuantity(controller.getQuantity());
	}

	private void handleNormalEdit(Food selectedFood) throws IOException {
		// Create window
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(getClass().getResource("/view/dietTabEditFoodWindow.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 352, 156);
		Stage stage = new Stage();
		Stage parent = (Stage) btnEdit.getScene().getWindow();
		stage.initOwner(parent);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setTitle("Edit Food");
		stage.setScene(scene);
		// Controller access
		EditFoodController controller = fxmlLoader.<EditFoodController>getController();
		controller.setTextFieldValue(Double.toString(selectedFood.getQuantity()));
		
		// showAndWait will block execution until the window closes...
		stage.showAndWait();

		// Update the selectedFood Quantity
		selectedFood.setQuantity(controller.getQuantity());
	}

	@FXML
	protected void handleCustom(ActionEvent event) throws IOException {
		try {
			// Create window
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/view/dietTabCustomFoodWindow.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 355, 275);
			Stage stage = new Stage();
			Stage parent = (Stage) btnCustom.getScene().getWindow();
			stage.initOwner(parent);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setTitle("Create Custom Food");
			stage.setScene(scene);

			// Controller access
			CustomFoodController controller = fxmlLoader.<CustomFoodController>getController();

			// showAndWait will block execution until the window closes...
			stage.showAndWait();

			// continue with the controller
			addCustom(controller);
		} catch (Exception e) {
			System.out.println("Couldn't make create food window..?");
		}
	}

	private void addCustom(CustomFoodController controller) {
		// Add values to the local database/memory table
		if (controller.valid()) {
			// Get Food from controller
			Food retFood = controller.getFood();
			
			// Copy it
			Food newFood = new Food(retFood.getName(), retFood);
			
			// Get rid of the other one
			retFood = null;

			// Add it to DietTabController table (if we selected to)
			if (controller.addToTable()) {
				addedFoods.add(newFood);
				foodData.add(newFood);
				currentDay.addFood(newFood);
			}else {
				// Add to the AddFoodController instead make sure its set as a template
				newFood.setTemplate(true);
			}
			
			AddFoodController.addedFoods.add(newFood);
			AddFoodController.foodData.add(newFood);
			update();
		}
	}

	@FXML
	protected void handleAddEntry(ActionEvent event) throws IOException {
		// Open a window which has a search bar to search for foods on the database
		System.out.println("Create add food window here");

		try {

			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/view/dietTabAddFoodWindow.fxml"));
			Scene scene = new Scene(fxmlLoader.load(), 600, 400);
			Stage stage = new Stage();
			Stage parent = (Stage) btnAddEntry.getScene().getWindow();
			stage.initOwner(parent);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.setTitle("Add Entry");
			stage.setScene(scene);

			// Setup controller
			AddFoodController controller = fxmlLoader.<AddFoodController>getController();

			// showAndWait will block execution until the window closes...
			stage.showAndWait();

			// continue on adding entry with controller
			addEntry(controller);

		} catch (IOException e) {
			System.out.println("Failed to create a window");
		}
	}

	private void addEntry(AddFoodController controller) {
		try {
			System.out.println("DietTabController: " + controller.getFood().getName());
			boolean found = false;
			// Check if this food already exists in the table, if it does increase its
			// quantity instead
			
			
			for (int i = 0; i < currentDay.getFoods().size(); i++) {
				// Assumes we don't have foods with exactly the same name.. (try adding id in
				// later)
				if (currentDay.getFoods().get(i).getName().equals(controller.getFood().getName())) {
					found = true;
					
					System.out.println("Updating quantity on addEntry");
					
					currentDay.getFoods().get(i).setQuantity(currentDay.getFoods().get(i).getQuantity() + controller.getQuantity());
					break;
				}
			}

			// Add a new row entry if same food isn't already added
			if (!found) {
				System.out.println("No Food was found, creating a new entry here!");
				
				// Copy the object
				Food newFood = new Food(controller.getFood().getName(), controller.getFood());
				double quantity = controller.getQuantity();
				newFood.setQuantity(quantity);
				
				
				// Add values to the table!
				addedFoods.add(newFood);
				foodData.add(newFood);
				currentDay.addFood(newFood);
			}

			// Update GUI
			update();
		} catch (NullPointerException e) {
			System.out.println("Nullpointerexception, probably because we hit the X");
		}
	}

	@FXML
	protected void handleDeleteEntry(ActionEvent event) throws IOException {
		// Make sure we selected an entry on the table
		try {
			// Get the current selected food before we edit it so we can update its quantity
			// later
			Food selectedFood = tvEntries.getSelectionModel().getSelectedItem();

			addedFoods.remove(selectedFood);
			foodData.remove(selectedFood);
			currentDay.deleteFood(selectedFood);

			update();
		} catch (NullPointerException e) {
			System.out.println("Couldn't delete item, probably haven't selected anything");
		}
	}
}