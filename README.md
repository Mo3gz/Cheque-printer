# Bilingual Cheque Printing Application

A comprehensive JavaFX-based cheque printing application with full bilingual support (Arabic and English), featuring live preview, template management, and database integration.

## Features

### 🌐 Bilingual Support
- Full Arabic and English text support
- Right-to-left (RTL) text rendering for Arabic
- Automatic Arabic text shaping and BiDi processing using ICU4J
- Arabic number-to-words conversion for amounts

### 📄 Cheque Generation
- **Single Cheque Creation**: Generate individual cheques with live preview
- **Multiple Cheque Generation**: Create sequential cheques with configurable intervals (Daily, Weekly, Monthly)
- **Live Preview**: Real-time visual preview of cheque layout as you type
- **Template Support**: Multiple bank templates with configurable field positioning

### 💾 Database Management
- SQLite database for persistent storage of cheque records
- Full CRUD operations (Create, Read, Update, Delete)
- Advanced filtering by beneficiary name and date range
- Historical record management with table view

### 🎨 User Interface
- Modern JavaFX interface with tabbed layout
- **Check Creation Tab**: Form-based data entry with live preview
- **Check Records Tab**: Table view with filtering and management options
- Responsive design with proper Arabic text alignment

## Technology Stack

- **JavaFX 17**: Modern desktop GUI framework
- **Maven**: Project management and dependency management
- **PDFBox**: PDF generation and manipulation
- **SQLite**: Lightweight embedded database
- **Gson**: JSON processing for configuration files
- **ICU4J**: Advanced internationalization and Arabic text processing

## Installation & Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building and Running the Application

1. **Clone/Download the project**
2. **Navigate to the project directory**
3. **Compile the project:**
   ```bash
   mvn clean compile
   ```
4. **Run the application:**
   ```bash
   mvn javafx:run
   ```

### Alternative Execution
```bash
# Create executable JAR
mvn clean package

# Run the JAR (requires JavaFX runtime)
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/pdfGenerator-1.0-SNAPSHOT.jar
```

## Usage Guide

### 1. Check Creation Tab

#### Basic Information
1. **Select Date**: Choose the cheque date using the date picker
2. **Enter Beneficiary**: Fill in the beneficiary name (supports both Arabic and English)
3. **Enter Amount**: Input the numeric amount (automatically converts to Arabic words)
4. **Enter Signer**: Fill in the signer's name

#### Single Cheque Generation
- Click "Print and Save Single Check" to generate and save one cheque
- The cheque will be saved to the database and can be viewed in the Records tab

#### Multiple Cheque Generation
1. **Set Number of Checks**: Enter how many cheques to generate
2. **Select Interval**: Choose between Daily, Weekly, or Monthly intervals
3. **Click "Print and Save Multiple Checks"**: Generates sequential cheques with incremented dates

### 2. Check Records Tab

#### Viewing Records
- All saved cheques are displayed in the table
- Records show ID, Date, Beneficiary, Amount, Amount in Words, and Signer

#### Filtering Records
- **By Beneficiary**: Type in the filter field to search by beneficiary name
- **By Date Range**: Select start and end dates, then click "Apply Date Filter"
- **Clear Filters**: Reset all filters to show all records

#### Managing Records
- **Delete Records**: Select a record and click "Delete Selected" to remove it
- **Double-click**: Double-click a record to populate the form with its data

## Project Structure

```
src/main/
├── java/org/chequePrinter/
│   ├── App.java                           # JavaFX Application Entry Point
│   ├── controller/
│   │   ├── SimpleController.java          # Main UI Controller (Active)
│   │   └── ChequeController.java          # Alternative Controller
│   ├── model/
│   │   ├── ChequeData.java               # Cheque data model
│   │   └── BankTemplate.java             # Bank template configuration
│   ├── service/
│   │   ├── DatabaseService.java          # SQLite database operations
│   │   ├── JsonLoader.java               # JSON configuration loader
│   │   └── PdfService.java               # PDF orchestration service
│   ├── util/
│   │   └── ArabicNumberToWords.java      # Arabic number-to-words conversion
│   └── [Legacy PDF classes...]
└── resources/
    ├── org/chequePrinter/view/
    │   └── ChequeView.fxml               # JavaFX UI layout
    ├── config/
    │   └── bank.json                     # Bank configuration file
    ├── fonts/
    │   └── Amiri-Regular.ttf             # Arabic font
    └── templates/
        └── nbe_template.jpg              # Cheque template image
```

## Configuration

### Bank Templates
The application uses `src/main/resources/config/bank.json` to configure bank templates and field positions. You can modify this file to add new banks or adjust field positioning.

### Database
The SQLite database (`cheques.db`) is automatically created in the project root directory on first run.

## Troubleshooting

### Common Issues
1. **JavaFX Runtime Issues**: Ensure JavaFX is properly installed and configured
2. **Font Issues**: The Arabic font (Amiri-Regular.ttf) must be present in the resources/fonts directory
3. **Database Issues**: Ensure write permissions in the project directory for SQLite database creation

### Arabic Text Display
- If Arabic text appears incorrectly, ensure the ICU4J library is properly loaded
- The application automatically handles Arabic text shaping and BiDi processing

## Features Implemented

✅ Bilingual Arabic/English support  
✅ Live preview with real-time updates  
✅ SQLite database integration  
✅ Single and multiple cheque generation  
✅ Date interval configuration  
✅ Record filtering and management  
✅ Arabic number-to-words conversion  
✅ Template-based cheque layout  
✅ Proper Arabic text rendering  
✅ Modern JavaFX interface  

## License

This project is created as a comprehensive cheque printing solution with bilingual support.