package org.hisp.dhis.ivb.covid.action;

import static org.hisp.dhis.commons.util.TextUtils.getCommaDelimitedString;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.Section;
import org.hisp.dhis.dataset.SectionService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.ivb.util.GenericDataVO;
import org.hisp.dhis.ivb.util.GenericTypeObj;
import org.hisp.dhis.ivb.util.IVBUtil;
import org.hisp.dhis.lookup.Lookup;
import org.hisp.dhis.lookup.LookupService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author BHARATH
 */

@Transactional
public class CampaignHelper
{
	public static final String REPORT_TEMPLATE_FOLDER = "report_templates";
	public static final String REPORT_TEMPLATE_ISC_SINGLE_COUNTRY = "ISC_SingleCountry.xlsx";
	
	private static final int processScoreCardDeIds[][] = {
				{1181662,1181663,1181664,1181665}, //Leadership
				{1181666,1181667,1181668,1181669}, //Continuous Improvement Plans
				{1181670,1181671,1181672,1181673}, //Data for Management
				{1181674,1181675,1181676,1181677}, //Cold Chain Equipment
				{1181678,1181679,1181680,1181681} //System Design
			};				
	
	private static final int pdbStatusDeId = 1181682;
	
	private static final int preformanceDashboardDeIds[][] = {
				{1181683,1181684,1181685,1181686,1181687,1181688,1181689,1181690,1181691},
				{1181692,1181693,1181694,1181695,1181696,1181697,1181698,1181699,1181700}
			};
	
	private static final String deIdsByComma = "1181662,1181663,1181664,1181665,1181666,1181667,1181668,1181669,1181670,1181671,1181672,1181673,1181674,1181675,1181676,1181677,1181678,1181679,1181680,1181681,1181682,1181683,1181684,1181685,1181686,1181687,1181688,1181689,1181690,1181691,1181692,1181693,1181694,1181695,1181696,1181697,1181698,1181699,1181700";
	
	private static Map<String, Integer> levelColMap;
	static {
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		
		tempMap.put("Level 1", 2);
		tempMap.put("Level 2", 3);
		tempMap.put("Level 3", 4);
		tempMap.put("Level 4", 5);
		tempMap.put("Level 5", 6);
		
		levelColMap = Collections.unmodifiableMap(tempMap);
	}
	
	private static final Map<String,String> processScoreCardTextIndexMap;
	static {
		Map<String, String> tempMap2 = new HashMap<String, String>();
		
		//Leadership 
		tempMap2.put("6:2", "N-0-45;B-45-57;N-57-113"); //Level1
		tempMap2.put("6:3", "N-0-55;B-55-65;N-65-79"); //Level2
		tempMap2.put("6:4", "B-0-11;N-11-115"); //Level3
		tempMap2.put("6:5", "N-0-22;B-22-31;N-31-69;B-69-106;N-106-140"); //Level4
		tempMap2.put("6:6", "N-0-34;B-34-49;N-49-129"); //Level5
		
		//Continuous Improvement Plans
		//tempMap2.put("7:2", "N-0-32;B-32-44;N-44-100"); //Level1
		tempMap2.put("7:2", "N-0-32;B-32-37;N-37-60"); //Level1
		tempMap2.put("7:3", "N-0-20;B-20-28;N-28-48;B-48-62;N-62-99"); //Level2
		tempMap2.put("7:4", "N-0-30;B-30-43;N-43-88;B-88-114"); //Level3
		tempMap2.put("7:5", "N-0-40;B-40-53;N-53-101;B-101-111;N-111-154"); //Level4
		tempMap2.put("7:6", "N-0-20;B-20-44;N-44-49;B-49-67;N-67-152;B-152-168;N-168-236"); //Level5
		
		//Data for Management
		tempMap2.put("8:2", "N-0-25;B-25-30;N-30-149"); //Level1
		tempMap2.put("8:3", "N-0-55;B-55-68;N-68-102;B-102-118;N-118-149"); //Level2
		tempMap2.put("8:4", "N-0-70;B-70-79"); //Level3
		tempMap2.put("8:5", "N-0-55;B-55-70;N-70-91"); //Level4
		tempMap2.put("8:6", "N-0-11;B-11-30;N-30-135"); //Level5
		
		//Cold Chain Equipment
		tempMap2.put("9:2", "N-0-19;B-19-26;N-26-40;B-40-45;N-45-55;B-55-83;N-83-96;B-96-102"); //Level1
		tempMap2.put("9:3", "N-0-19;B-19-24;N-24-34;B-34-60;N-60-119"); //Level2
		tempMap2.put("9:4", "N-0-12;B-12-22;N-22-114;B-114-121;N-121-151"); //Level3
		tempMap2.put("9:5", "N-0-59;B-59-69;N-69-220"); //Level4
		tempMap2.put("9:6", "N-0-12;B-12-35;N-35-94;B-94-141;N-141-150"); //Level5
		
		//System Design
		tempMap2.put("10:2", "N-0-23;B-23-34;N-34-108"); //Level1
		tempMap2.put("10:3", "N-0-4;B-4-20;N-20-131"); //Level2
		tempMap2.put("10:4", "N-0-67;B-67-77;N-77-99;B-99-124"); //Level3
		tempMap2.put("10:5", "N-0-4;B-4-14;N-14-46;B-46-58"); //Level4
		tempMap2.put("10:6", "N-0-58;B-58-77;N-77-85"); //Level5

		processScoreCardTextIndexMap = Collections.unmodifiableMap(tempMap2);
	}
	// -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
	private JdbcTemplate jdbcTemplate;
    public void setJdbcTemplate( JdbcTemplate jdbcTemplate ){
        this.jdbcTemplate = jdbcTemplate;
    }
    
	@Autowired
    private IVBUtil ivbUtil;
	
	@Autowired
	private SectionService sectionService;
	
    @Autowired 
    private LookupService lookupService;

    @Autowired
    private OrganisationUnitGroupService ouGroupService;
    
    @Autowired
    private ProgramStageService programStageService;

    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private DataElementCategoryService categoryService;
    
    @Autowired
    private DataValueService dataValueService;
    
    @Autowired
    private OrganisationUnitService ouService;
    //--------------------------------------------------------------------------
    // Input/Output
    //--------------------------------------------------------------------------

    
    //---------------------------------------------------------------------------
    // Methods
    //---------------------------------------------------------------------------
    public Workbook generateISCExcel( OrganisationUnit orgUnit )
    {
    	Map<String, DataValue> dataValueMap = new HashMap<String, DataValue>();
    	
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	
    	try {    		    		 
    		SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yy");
    		
    		int ouId = orgUnit.getId();
    		dataValueMap = ivbUtil.getLatestDataValuesForTabularReport( deIdsByComma, ouId+"" );
    		
	    	String templatePath = System.getenv( "DHIS2_HOME" ) + File.separator + REPORT_TEMPLATE_FOLDER + File.separator + REPORT_TEMPLATE_ISC_SINGLE_COUNTRY;
	    	
	    	FileInputStream templateExcelFile = new FileInputStream(new File(templatePath));
	    	workbook = new XSSFWorkbook(templateExcelFile);
	    	XSSFSheet sheet0 = workbook.getSheetAt(0);
	    	XSSFSheet sheet1 = workbook.getSheetAt(1);

	    	
	    	XSSFRow row = sheet0.getRow( 6 );
	    	XSSFCell cell = row.getCell( 2 );
	    	XSSFCellStyle defaultCellStyle = cell.getCellStyle();
        	

	    	XSSFFont font1 = workbook.createFont();
	    	font1.setFontName( "Calibri" );
	    	font1.setFontHeightInPoints( (short) 9 );
	    	font1.setItalic( false );
	    	font1.setColor( IndexedColors.BLACK.index );
	    	
	    	XSSFFont font2 = workbook.createFont();
	    	font2.setFontName( "Calibri" );
	    	font2.setFontHeightInPoints( (short) 9 );
	    	font2.setItalic( false );
	    	font2.setBold( true );
	    	font2.setColor( IndexedColors.BLACK.index );
	    		    	
	    	
	    	XSSFCellStyle baseLevelCellStyle = (XSSFCellStyle) defaultCellStyle.clone();
	    	baseLevelCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(216, 228, 188)));
	    	baseLevelCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	    	
	    	XSSFCellStyle currentLevelCellStyle = (XSSFCellStyle) defaultCellStyle.clone();
	    	currentLevelCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(196, 215, 155)));
	    	currentLevelCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

	    	
	    	XSSFCellStyle targetLevelCellStyle = (XSSFCellStyle) defaultCellStyle.clone();
	    	targetLevelCellStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(155, 187, 89)));
	    	targetLevelCellStyle.setFillPattern(FillPatternType.THIN_FORWARD_DIAG);
	        
	    	
	    	XSSFCellStyle[] levelStyles = new XSSFCellStyle[] { baseLevelCellStyle, currentLevelCellStyle, targetLevelCellStyle };
	    	
	    	//Sheet0: Process Scorecard - Country Name
	    	row = sheet0.getRow( 3 );
	    	cell = row.getCell( 2 );
	        cell.setCellValue( orgUnit.getName() );

	        int rowNo = 6;
	        for( int i = 0; i < processScoreCardDeIds.length; i++ ) {
	        	row = sheet0.getRow( rowNo+i );
	        	//System.out.println( "Row = "+ (rowNo+i+1));
	        	for( int i2 = 0; i2 < 3; i2++) {
			        DataValue dv = dataValueMap.get( ouId+":"+processScoreCardDeIds[i][i2] );
			        if( dv != null && dv.getValue() != null ) {
			        	cell = row.getCell( levelColMap.get(dv.getValue()) );
			        	XSSFRichTextString richText = cell.getRichStringCellValue();		        	
			        	String tempKey = cell.getRowIndex()+":"+cell.getColumnIndex();
			        	//System.out.println( "richText = " + richText );
			        	//System.out.println( tempKey + ",   " + processScoreCardTextIndexMap.get( tempKey ) );
			        	if( processScoreCardTextIndexMap.get( tempKey ) != null ) {
			        		String richTextIndexes = processScoreCardTextIndexMap.get( tempKey );
			        		for( int j = 0; j < richTextIndexes.split(";").length; j++) {
			        			String styleCat = richTextIndexes.split(";")[j].split("-")[0];
			        			int sIndex = Integer.parseInt( richTextIndexes.split(";")[j].split("-")[1] );
			        			int eIndex = Integer.parseInt( richTextIndexes.split(";")[j].split("-")[2] );
			        			
			        			if( styleCat.equals("N") ) {
				        			richText.applyFont(sIndex, eIndex, font1);		        				
			        			}
			        			else {
				        			richText.applyFont(sIndex, eIndex, font2);
			        			}
			        		}
			        	}			        				        	
			        	cell.setCellValue( richText );
			        	cell.setCellStyle( levelStyles[i2] );
			        }
	        	}
	        		        	
	        	DataValue dv = dataValueMap.get( ouId+":"+processScoreCardDeIds[i][3] );
		        if( dv != null && dv.getValue() != null ) {
		        	cell = row.getCell( 7 );
		        	cell.setCellValue( dv.getValue() );
		        }
		        //System.out.println("Degree: "+ ((dv != null)? dv.getValue() : "no data"));
	        }
	        
	        //---------------------------------------------------------------------------------------
	        //Sheet1: Performance Dashboard
	        //---------------------------------------------------------------------------------------
	        //Country Name
	    	row = sheet1.getRow( 3 );
	    	cell = row.getCell( 2 );
	        cell.setCellValue( orgUnit.getName() );
	        /*cell = row.getCell( 4 );
	        DataValue dv = dataValueMap.get( ouId+":"+pdbStatusDeId );
	        if( dv != null && dv.getValue() != null ) {
	        	cell.setCellValue( dv.getValue() );
	        }*/
	        
	        row = sheet1.getRow( 5 );
	    	cell = row.getCell( 4 );
	    	DataValue dv = dataValueMap.get( ouId+":"+pdbStatusDeId );
	        if( dv != null && dv.getValue() != null ) {
	        	cell.setCellValue( dv.getValue() );
	        }
	        //System.out.println( "Status dv value = " + dv.getValue() );
	        
	        rowNo = 8;
	        for( int i=0; i < preformanceDashboardDeIds.length; i++ ) {
	        	row = sheet1.getRow( ++rowNo+i );
	        	
	        	//Time Frame
	        	String timeFrame = "", monYearCollected = "NA";
	        	cell = row.getCell( 2 );
		        dv = dataValueMap.get( ouId+":"+preformanceDashboardDeIds[i][0] );
		        if( dv != null && dv.getValue() != null ) {
		        	timeFrame = dv.getValue();
		        	monYearCollected = monthFormat.format( dv.getLastUpdated() );
		        }
		        else
		        	timeFrame = "NA";
		        
		        dv = dataValueMap.get( ouId+":"+preformanceDashboardDeIds[i][1] );
		        if( dv != null && dv.getValue() != null ) {
		        	timeFrame += " to " + dv.getValue();
		        	monYearCollected = monthFormat.format( dv.getLastUpdated() );
		        }
		        else
		        	timeFrame += " to NA";
		        cell.setCellValue( timeFrame );
		        
		        //Month Year Collected
		        cell = row.getCell( 3 );
		        cell.setCellValue( monYearCollected );
		        
		        //Remaining Columns
		        int colNo = 4;
	        	for(int j=2; j < preformanceDashboardDeIds[i].length; j++) {
	        		cell = row.getCell( colNo++ );	        		
	        		dv = dataValueMap.get( ouId+":"+preformanceDashboardDeIds[i][j] );
			        if( dv != null && dv.getValue() != null ) {			        	
			        	try{ cell.setCellValue( Integer.parseInt(dv.getValue() ) ); }catch(Exception e) { cell.setCellValue( dv.getValue() ); }
			        }			        
	        	}
	        }

    	}
    	catch(Exception e){
    		System.out.println("Exception in generateISCExcel: "+e.getMessage() );
    		e.printStackTrace();
    	}
    	return workbook;
    }
   
    //OrgUnitId + "_" + DataElementId as key and data as value
    public Map<String, GenericDataVO> getLatestDataValues( String deIdsByComma, String ouIdsByComma )
    {
        Map<String, GenericDataVO> latestDataValues = new HashMap<String, GenericDataVO>();
        
        try
        {
            String query = "SELECT dv.sourceid, dv.dataelementid, dv.periodid, dv.value, dv.comment, dv.storedby, dv.lastupdated " +
                    " FROM " +
                        "( " +
                            " SELECT periodid,dataelementid,sourceid FROM " + 
                                "(SELECT MAX(p.startdate) AS startdate,dv.dataelementid,dv.sourceid FROM datavalue dv " +
                                    " INNER JOIN period p ON p.periodid=dv.periodid " +
                                        " WHERE dv.dataelementid IN ( "+ deIdsByComma +") AND dv.sourceid IN (" + ouIdsByComma + ")  " + 
                                        " GROUP BY dv.dataelementid,dv.sourceid " +
                                 ")asd " +
                             " INNER JOIN period p ON p.startdate=asd.startdate " +
                         ")asd1 " +
                     " INNER JOIN datavalue dv ON dv.sourceid=asd1.sourceid " +
                     " AND dv.dataelementid=asd1.dataelementid " +
                     " AND dv.periodid=asd1.periodid";

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while( rs.next() ){
                Integer ouId = rs.getInt( 1 );
                Integer deId = rs.getInt( 2 );
                Integer pId = rs.getInt( 3 );
                String value = rs.getString( 4 );
                String comment = rs.getString( 5 );
                String storedBy = rs.getString( 6 );
                Date lastUpdated = rs.getDate( 7 );

                GenericDataVO dataVo = new GenericDataVO();
                dataVo.setStrVal1( value );
                dataVo.setStrVal2( comment );
                latestDataValues.put( ouId+"_"+deId, dataVo );
            }
        }
        catch( Exception e ){
        	e.printStackTrace();
        }
        
        return latestDataValues;
    }
    
    
    public Map<String, GenericDataVO> getLatestDataValuesForCampaignReport( String deIdsByComma, String ouIdsByComma )
    {
        Map<String, GenericDataVO> latestDataValues = new HashMap<String, GenericDataVO>();
        
        try
        {
            String query = "SELECT dv.sourceid, dv.dataelementid, dv.periodid, dv.value, dv.comment, dv.storedby, dv.lastupdated " +
                    " FROM " +
                        "( " +
                            " SELECT periodid,dataelementid,sourceid FROM " + 
                                "(SELECT MAX(p.startdate) AS startdate,dv.dataelementid,dv.sourceid FROM datavalue dv " +
                                    " INNER JOIN period p ON p.periodid=dv.periodid " +
                                        " WHERE dv.dataelementid IN ( "+ deIdsByComma +") AND dv.sourceid IN (" + ouIdsByComma + ")  " + 
                                        " GROUP BY dv.dataelementid,dv.sourceid " +
                                 ")asd " +
                             " INNER JOIN period p ON p.startdate=asd.startdate " +
                         ")asd1 " +
                     " INNER JOIN datavalue dv ON dv.sourceid=asd1.sourceid " +
                     " AND dv.dataelementid=asd1.dataelementid " +
                     " AND dv.periodid=asd1.periodid " +
                     " WHERE dv.value IS NOT NULL AND dv.value <> ''";
            
            //System.out.println(query);

            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while( rs.next() ){
                Integer ouId = rs.getInt( 1 );
                Integer deId = rs.getInt( 2 );
                Integer pId = rs.getInt( 3 );
                String value = rs.getString( 4 );
                String comment = rs.getString( 5 );
                String storedBy = rs.getString( 6 );
                Date lastUpdated = rs.getDate( 7 );

                GenericDataVO dataVo = new GenericDataVO();
                dataVo.setStrVal1( value );
                dataVo.setStrVal2( comment );
                latestDataValues.put( ouId+"_"+deId, dataVo );
            }
        }
        catch( Exception e ){
        	e.printStackTrace();
        }
        
        return latestDataValues;
    }
    
    
    //programstageid +"_" + OrgUnitId as key and 
    //	value is hashmap where key is programstage instance id and value is CampaignVO
    public Map<String, Map<Integer, CampaignVO>> getEventData( String psIdsByComma, String deIdsByComma, String ouIdsByComma )
    {
    	Map<String, Map<Integer, CampaignVO>> eventDataMap = new HashMap<>();
        
        try
        {
            String query = "SELECT t2.programstageid, t1.programstageinstanceid, t2.organisationunitid, t1.dataelementid, t1.value FROM trackedentitydatavalue as t1 " + 
            					" INNER JOIN programstageinstance as t2 ON t1.programstageinstanceid = t2.programstageinstanceid " + 
            					" WHERE t2.programstageid IN ("+ psIdsByComma +") AND t2.organisationunitid IN ("+ ouIdsByComma +") AND t1.dataelementid IN ("+ deIdsByComma +")"; 

            //System.out.println("getEventData Query= "+query);
            SqlRowSet rs = jdbcTemplate.queryForRowSet( query );
            
            while( rs.next() ){
                Integer psId = rs.getInt( 1 );
                Integer psInstId = rs.getInt( 2 );
            	Integer ouId = rs.getInt( 3 );
                Integer deId = rs.getInt( 4 );
                String value = rs.getString( 5 );

                String baseKey = psId+"_"+ouId;
                if( eventDataMap.get( baseKey) == null )
                	eventDataMap.put(baseKey, new HashMap<>());
                
                if( eventDataMap.get( baseKey).get(psInstId) == null )
                	eventDataMap.get( baseKey).put(psInstId, new CampaignVO());
                
                if( eventDataMap.get( baseKey).get(psInstId).getColDataMap() == null )
                	eventDataMap.get( baseKey).get(psInstId).setColDataMap( new HashMap<>());
                
                GenericDataVO dvo = new GenericDataVO();
                dvo.setStrVal1(value);
                eventDataMap.get( baseKey).get(psInstId).getColDataMap().put(deId+"", dvo);
            }
        }
        catch( Exception e ){
        	e.printStackTrace();
        }
        
        return eventDataMap;
    }
   
    public CampaignSnapshot getCampainTrackerSnap(CampaignSnapshot campaignSnap )
    {
    	String ouIdsByComma = "-1";
        if ( campaignSnap.getOuIds() != null && campaignSnap.getOuIds().size() > 0 ){
        	ouIdsByComma = getCommaDelimitedString( campaignSnap.getOuIds() );
        }
        Lookup lookup = lookupService.getLookupByName( "UNICEF_REGIONS_GROUPSET" );
        campaignSnap.setUnicefRegionsGroupSet( ouGroupService.getOrganisationUnitGroupSet( Integer.parseInt( lookup.getValue() ) ) );
        for(Integer ouId : campaignSnap.getOuIds() ){
            OrganisationUnit orgUnit = ouService.getOrganisationUnit( ouId );
            campaignSnap.getOrgUnitList().add( orgUnit );
        }
        Collections.sort(campaignSnap.getOrgUnitList(), new IdentifiableObjectNameComparator() );
        
        //System.out.println(ouIdsByComma);
        //Selected Campaigns 
        Set<String> sectionCodes = new HashSet<>();
        for( Integer sectionId : campaignSnap.getCampaignIds() ){
            Section section = sectionService.getSection( sectionId );
            campaignSnap.getDsSections().add( section );
            
            sectionCodes.add( section.getCode().trim().toLowerCase() );
        }

        
        lookup = lookupService.getLookupByName( "CAMPAIGN_PROGRAM_STAGE_IDS" );
        String psIdsByComma = "-1";
        List<ProgramStage> programStages = new ArrayList<>();
        for(String psId : lookup.getValue().split(",")) {
        	ProgramStage ps = programStageService.getProgramStage( Integer.parseInt(psId) );
        	if( ps!= null && sectionCodes.contains( ps.getName().trim().toLowerCase()) ) {
        		programStages.add( ps );
        		psIdsByComma += ","+ps.getId();
        	}
        }
        
        lookup = lookupService.getLookupByName( "CAMPAIGN_SUBNATIONAL_DEID" );
        int subNationalDeId = Integer.parseInt( lookup.getValue() );
        lookup = lookupService.getLookupByName( "CAMPAIGN_GEOGRAPHIC_SCALE_DEID" );
        int geoScaleDeId = Integer.parseInt( lookup.getValue() );
        
        
        //Selected Columns
        Map<Integer, String> deColMap = new HashMap<>();
        String deIdsByComma = "-1";
        String psDeIdsByComma = "-1";
        psDeIdsByComma += "," +subNationalDeId;
        lookup = lookupService.getLookupByName( "CAMPAIGN_COLUMNS_INFO" );
        String campaignColInfo = lookup.getValue();
        //colList = new ArrayList<GenericTypeObj>();
        for( String colInfo : campaignColInfo.split("@!@") ) {
        	if( campaignSnap.getSelCols().contains( colInfo.split("@-@")[0] ) ) {
	        	GenericTypeObj colObj = new GenericTypeObj();
	        	colObj.setCode( colInfo.split("@-@")[0] );
	        	colObj.setName( colInfo.split("@-@")[1] );
	        	colObj.setStrAttrib1( colInfo.split("@-@")[2] ); //deids
	        	colObj.setStrAttrib2( colInfo.split("@-@")[3] ); //ps deids
	        	campaignSnap.getColList().add( colObj );
	        	deIdsByComma += ","+colInfo.split("@-@")[2];
	        	psDeIdsByComma += ","+colInfo.split("@-@")[3];
	        	for(String deIdStr : colObj.getStrAttrib1().split(",") ) {
	        		deColMap.put(Integer.parseInt(deIdStr), colObj.getCode());
	        	}
	        	deColMap.put(Integer.parseInt(colObj.getStrAttrib2()), colObj.getCode());
        	}
        }
        
        
        //System.out.println(deIdsByComma);
        Map<String, GenericDataVO> dvDataMap = getLatestDataValuesForCampaignReport( deIdsByComma, ouIdsByComma );
        Map<String, Map<Integer, CampaignVO>> eventDataMap = getEventData( psIdsByComma, psDeIdsByComma, ouIdsByComma );
        //System.out.println( dvDataMap.size() +" and "+eventDataMap.size() );
        
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        DecimalFormat df = new DecimalFormat( "#,###,###,##0" );
        
        campaignSnap.setNf( nf );
        
        //Arranging Aggregated Data
        Set<Integer> deIds = deColMap.keySet();
        Map<String, List<CampaignVO>> dataMap = new HashMap<>();
        String subNationName = "NONE";
        Set<String> subNationalNames = new HashSet<>();
        for( int ouId : campaignSnap.getOuIds() ) {
        	
        	//String key1 = ouId+"_National";
			//dataMap.put(key1, new ArrayList<>());
        	for(Section section : campaignSnap.getDsSections() ) {
        		subNationName = "NONE";
        		String key1 = ouId+"_"+section.getCode();
        		if( dataMap.get(key1) == null)
        			dataMap.put(key1, new ArrayList<>());
        		int flag = 0;
        		Map<String, GenericDataVO> vacDataMap = null;
        		
        		//String key = ouId+"_"+section.getId();
        		for( DataElement de : section.getDataElements()) {
        			if( deIds.contains( de.getId() ) ){
	        			String dvKey = ouId+"_"+de.getId();
	        			String colCode = "NONE";
	        			colCode = deColMap.get(de.getId());
	        			if( dvDataMap.get(dvKey)!= null ) {
	        				if( vacDataMap == null )
	        					vacDataMap = new HashMap<>();
	        				flag = 1;
	        				
	        				vacDataMap.put(colCode, dvDataMap.get(dvKey));
	        				if( colCode.trim().equals("COL_4") )
	        					vacDataMap.put("COL_3", dvDataMap.get(dvKey));
	        				else if( colCode.trim().equals("COL_8") ) {
	        					String val = "";
	        					try{ val = dvDataMap.get(dvKey).getStrVal1();}catch(Exception e) {}
	        					try{ val = df.format(Double.parseDouble(val));}catch(Exception e) {}
	        					if( !val.trim().equals("") )
	        						dvDataMap.get(dvKey).setStrVal1( val );
	        					vacDataMap.put(colCode, dvDataMap.get(dvKey));
	        				}
	        				else if( colCode.trim().equals("COL_10") ) {
	        					String val = "";
	        					try{ val = dvDataMap.get(dvKey).getStrVal1();}catch(Exception e) {}
	        					if( !val.trim().equals("") )
	        						subNationName = val;
	        				}
	        					
							//System.out.println(key + " + " + colCode +" = " +dvDataMap.get(key2).getStrVal1());
	        			}
        			}
        		}
        		
        		if( flag == 1) {
        			CampaignVO cvo = new CampaignVO();
        			GenericDataVO dvo = new GenericDataVO();
            		dvo.setStrVal1(subNationName);
            		dvo.setIntVal1(1);
            		vacDataMap.put("COL_0", dvo);
            		cvo.setColDataMap( vacDataMap );
            		dataMap.get(key1).add( cvo );
            		subNationalNames.add( subNationName );
        		}
        	}
        }
        
        
        
        //Arranging Event Data
        for( int ouId : campaignSnap.getOuIds() ) {
        	for(ProgramStage ps : programStages ) {
        		String eBaseKey = ps.getId()+"_"+ouId;
        		if( eventDataMap.get(eBaseKey) == null ) {
        			//System.out.println("Data Unavailable for the key "+ eBaseKey);
        			continue;
        		}
        		else {
        			//System.out.println("Data available for the key "+ eBaseKey);
        		}
        		for(Integer psInsId : eventDataMap.get(eBaseKey).keySet()) {
        			subNationName = "NONE";
        			try { subNationName = eventDataMap.get(eBaseKey).get(psInsId).getColDataMap().get(subNationalDeId+"").getStrVal1();}catch(Exception e) {}
        			if( subNationName.trim().equals("NONE") || subNationName.trim().equals("") ) {
        				try { subNationName = eventDataMap.get(eBaseKey).get(psInsId).getColDataMap().get(geoScaleDeId+"").getStrVal1();}catch(Exception e) {}
        			}
        				
        			int flag = 0;
            		//String key1 = ouId+"_"+subNationName;
        			String key1 = ouId+"_"+ps.getName();
            		//System.out.println( key1 );
            		if( dataMap.get(key1) == null)
            			dataMap.put(key1, new ArrayList<>());
            		Map<String, GenericDataVO> vacDataMap = null;
            		for( DataElement de : ps.getAllDataElements()) {
            			if( deIds.contains( de.getId() ) ){
    	        			String colCode = "NONE";
    	        			colCode = deColMap.get(de.getId());
    	        			GenericDataVO dvo = null;
    	        			try { dvo = eventDataMap.get(eBaseKey).get(psInsId).getColDataMap().get(de.getId()+"");}catch(Exception e) {}
    	        			if( dvo != null ) {
    	        				if( vacDataMap == null )
    	        					vacDataMap = new HashMap<>();
    	        				flag = 1;
    	        				if( colCode.trim().equals("COL_4") )
    	        					dvo.setStrVal2(dvo.getStrVal1());
    	        				else if( colCode.trim().equals("COL_8") ) {
    	        					String val = "";
    	        					try{ val = dvo.getStrVal1();}catch(Exception e) {}
    	        					try{ val = df.format(Double.parseDouble(val));}catch(Exception e) {}
    	        					if( !val.trim().equals("") )
    	        						dvo.setStrVal1( val );
    	        				}
    	        				vacDataMap.put(colCode, dvo);
    	        				subNationalNames.add(subNationName);
    	        			}
            			}
            		}
            		if( flag == 1) {
            			CampaignVO cvo = new CampaignVO();
            			GenericDataVO dvo = new GenericDataVO();
                		dvo.setStrVal1(subNationName );
                		dvo.setIntVal1(2);
                		vacDataMap.put("COL_0", dvo);
                		cvo.setColDataMap( vacDataMap );
                		dataMap.get(key1).add( cvo );
                		//System.out.println("Data Row added for PS instanceid : "+ psInsId );
            		}
        		}
        	}
        }		
       
        campaignSnap.getSubNatNames().addAll( subNationalNames );
        Collections.sort( campaignSnap.getSubNatNames() );
        
        campaignSnap.setCtDataMap(dataMap);
        
		/*
        for( String key1 : dataMap.keySet() ) {
        	for( CampaignVO cvo : dataMap.get(key1)) {
        		for(String key2 : cvo.getColDataMap().keySet() ) {
        			System.out.println( key1 + ", " + key2 + " = " + cvo.getColDataMap().get(key2).getStrVal1() );
        		}
        	}
        }
		*/
       
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        for( OrganisationUnit orgUnit : campaignSnap.getOrgUnitList() )
        {
            DataElement de1 = dataElementService.getDataElement( 4 );            
            DataValue dv = dataValueService.getLatestDataValue( de1, optionCombo, orgUnit );            
            if( dv == null || dv.getValue() == null )
            {
                campaignSnap.getCountryGeneralInfoMap().put( orgUnit.getId()+":4", "" );
            }
            else
            {
            	campaignSnap.getCountryGeneralInfoMap().put( orgUnit.getId()+":4", dv.getValue() );
            }

            de1 = dataElementService.getDataElement( 3 );
            dv = dataValueService.getLatestDataValue( de1, optionCombo, orgUnit );            
            if( dv == null || dv.getValue() == null )
            {
            	campaignSnap.getCountryGeneralInfoMap().put( orgUnit.getId()+":3", "" );
            }
            else
            {
            	campaignSnap.getCountryGeneralInfoMap().put( orgUnit.getId()+":3", dv.getValue() );
            }
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date();
        campaignSnap.setCurDateStr( sdf.format( curDate ) );
        
    	return campaignSnap;
    }
    
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle subHeaderStyle;
    private XSSFCellStyle tHeaderStyle;
    private XSSFCellStyle numberStyle;
    private XSSFCellStyle dataStyle;
    private void initialiseExcelProps( XSSFWorkbook wb )
    {
    	DataFormat dataFormat = wb.createDataFormat();
    	
    	Font headerFont = wb.createFont();//Create font
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);//Make font bold
		headerFont.setFontHeightInPoints((short)15);
		headerFont.setColor(IndexedColors.BLACK.getIndex());
	    
    	Font subHeaderFont = wb.createFont();//Create font
    	subHeaderFont.setBoldweight(Font.BOLDWEIGHT_BOLD);//Make font bold
    	subHeaderFont.setFontHeightInPoints((short)12);
    	subHeaderFont.setColor(IndexedColors.BLACK.getIndex());
	    
    	//String rgbS = "FFF000";
    	//byte[] rgbB = Hex.decodeHex(rgbS.toCharArray()); // get byte array from hex string
   	    
    	Font tHeaderFont = wb.createFont();//Create font
    	tHeaderFont.setFontHeightInPoints((short)10);
    	tHeaderFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    	tHeaderFont.setColor(IndexedColors.BLACK.getIndex());
 	    
		Font dataFont = wb.createFont();//Create font
	    dataFont.setFontHeightInPoints((short)10);
	    dataFont.setColor(IndexedColors.BLACK.getIndex());

	    
	    Font dataFontBoldItalic = wb.createFont();//Create font
	    dataFontBoldItalic.setFontHeightInPoints((short)10);
	    dataFontBoldItalic.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    dataFontBoldItalic.setItalic( true );
	    dataFontBoldItalic.setColor(IndexedColors.BLACK.getIndex());
	    
	    // Main Header
 		headerStyle = wb.createCellStyle();//Create style
 		headerStyle.setAlignment(CellStyle.ALIGN_LEFT);
 		//headerStyle.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.index );
 		//headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		//headerStyle.setWrapText( true );
 		headerStyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
 	    headerStyle.setFont(headerFont);
 	    
 	    //Sub Header
 		subHeaderStyle = wb.createCellStyle();//Create style
 		subHeaderStyle.setAlignment(CellStyle.ALIGN_LEFT);
 		subHeaderStyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
 		subHeaderStyle.setFont(subHeaderFont);
 	    
 	    //Table Header
 		tHeaderStyle = wb.createCellStyle();//Create style
 		tHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);
 		//tHeaderStyle.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.index );
 		XSSFColor color = new XSSFColor( Color.decode("#F0F0F0") );
 		tHeaderStyle.setFillForegroundColor( color );
 		tHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		tHeaderStyle.setWrapText( true );
 		tHeaderStyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
 		tHeaderStyle.setFont(tHeaderFont);
 	    
 		//Data Style
 		dataStyle = wb.createCellStyle();
	    dataStyle.setAlignment(CellStyle.ALIGN_LEFT);
	    dataStyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
	    dataStyle.setWrapText( true );
	    dataStyle.setFont( dataFont );
	    
 	    //Number Style
	    numberStyle = wb.createCellStyle();
	    numberStyle.setAlignment(CellStyle.ALIGN_RIGHT);
	    numberStyle.setDataFormat(dataFormat.getFormat("###,###0;[Red]-###,###0"));
	    numberStyle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
	    numberStyle.setFont( dataFont );

    }
    
    public Workbook generateCampaignTrackerXl( CampaignSnapshot campaignSnap )
    {
    	XSSFWorkbook workbook = new XSSFWorkbook();
    	try {
    		//CreationHelper factory = workbook.getCreationHelper();
			//Drawing drawing = sheet.createDrawingPatriarch();
			
    		initialiseExcelProps( workbook );
    		
    		int rowCount = 0;
    		
			Sheet sheet = workbook.createSheet( "CampaignTracker" );
			
			//Main Header
			Row row = sheet.createRow( rowCount++ );			
			int colCount = 0;
			Cell cell = row.createCell( colCount++ );
			cell.setCellStyle( headerStyle );
			//sheet.addMergedRegion( new org.apache.poi.ss.util.CellRangeAddress( rowCount-1, rowCount-1, colCount-1, colCount+5 ) );
			cell.setCellValue( "Campaign Tracker - current as of "+campaignSnap.getCurDateStr() );
			
			//Sub Header
			row = sheet.createRow( rowCount++ );			
			colCount = 0;
			cell = row.createCell( colCount++ );
			cell.setCellStyle( subHeaderStyle );
			//sheet.addMergedRegion( new org.apache.poi.ss.util.CellRangeAddress( rowCount-1, rowCount-1, colCount-1, colCount+5 ) );
			String campaigns = "";
			for( Section section : campaignSnap.getDsSections() )
				campaigns += section.getCode()+" "; 
			cell.setCellValue( "Campaigns: "+campaigns );
			
			rowCount++;
			
			//Table Header
			row = sheet.createRow( rowCount++ );			
			colCount = 0;			
			if( campaignSnap.getIsoCode() != null && campaignSnap.getIsoCode().trim().equals("ON") ) {
				cell = row.createCell( colCount++ );
				cell.setCellStyle( tHeaderStyle );
				cell.setCellValue( "ISO Code" );
				sheet.setColumnWidth(colCount-1, 8 * 256);

			}
			if( campaignSnap.getWhoRegion() != null && campaignSnap.getWhoRegion().trim().equals("ON") ) {
				cell = row.createCell( colCount++ );
				cell.setCellStyle( tHeaderStyle );
				cell.setCellValue( "WHO Region" );
				sheet.setColumnWidth(colCount-1, 15 * 256);
			}
			if( campaignSnap.getUnicefRegion() != null && campaignSnap.getUnicefRegion().trim().equals("ON") ) {
				cell = row.createCell( colCount++ );
				cell.setCellStyle( tHeaderStyle );
				cell.setCellValue( "UNICEF Region" );
				sheet.setColumnWidth(colCount-1, 10 * 256);
			}

			cell = row.createCell( colCount++ );
			cell.setCellStyle( tHeaderStyle );
			cell.setCellValue( "Country" );
			sheet.setColumnWidth(colCount-1, 25 * 256);
			
			if( campaignSnap.getIncomeLevel() != null && campaignSnap.getIncomeLevel().trim().equals("ON") ) {
				cell = row.createCell( colCount++ );
				cell.setCellStyle( tHeaderStyle );
				cell.setCellValue( "Income level" );
				sheet.setColumnWidth(colCount-1, 25 * 256);
			}
			if( campaignSnap.getGaviEligibleStatus() != null && campaignSnap.getGaviEligibleStatus().trim().equals("ON") ) {
				cell = row.createCell( colCount++ );
				cell.setCellStyle( tHeaderStyle );
				cell.setCellValue( "GAVI eligibility status" );
				sheet.setColumnWidth(colCount-1, 25 * 256);
			}

			cell = row.createCell( colCount++ );
			cell.setCellStyle( tHeaderStyle );
			cell.setCellValue( "Campaign Vaccine" );
			sheet.setColumnWidth(colCount-1, 15 * 256);
			
			cell = row.createCell( colCount++ );
			cell.setCellStyle( tHeaderStyle );
			cell.setCellValue( "Campaign Identifier" );
			sheet.setColumnWidth(colCount-1, 25 * 256);
			
			int freezeColNo = colCount;
			
			for( GenericTypeObj colObj : campaignSnap.getColList() ){
				cell = row.createCell( colCount++ );
				cell.setCellStyle( tHeaderStyle );
				cell.setCellValue( colObj.getName() );
				if( colObj.getCode().equals("COL_1") || colObj.getCode().equals("COL_2") || colObj.getCode().equals("COL_5") || colObj.getCode().equals("COL_6") ||
						colObj.getCode().equals("COL_7") || colObj.getCode().equals("COL_8") || colObj.getCode().equals("COL_10") )
					sheet.setColumnWidth(colCount-1, 15 * 256);
				else if( colObj.getCode().equals("COL_3") || colObj.getCode().equals("COL_9") || colObj.getCode().equals("COL_11") || colObj.getCode().equals("COL_12") )
					sheet.setColumnWidth(colCount-1, 25 * 256);
				else if( colObj.getCode().equals("COL_4") || colObj.getCode().equals("COL_13") )
					sheet.setColumnWidth(colCount-1, 40 * 256);
				else
					sheet.setColumnWidth(colCount-1, 20 * 256);
				if( campaignSnap.getShowComment() != null && campaignSnap.getShowComment().equals("ON") && !colObj.getCode().equals("COL_3") && !colObj.getCode().equals("COL_4") && !colObj.getCode().equals("COL_13") ) {
					cell = row.createCell( colCount++ );
					cell.setCellStyle( tHeaderStyle );
					cell.setCellValue( colObj.getName() +" Comment");

					sheet.setColumnWidth(colCount-1, 40 * 256);
				}
			}
			
			sheet.setAutoFilter(new CellRangeAddress(rowCount-1, rowCount-1, 0, colCount-1));
			sheet.createFreezePane( freezeColNo, rowCount);
			
			//Table Body - Data
			for( OrganisationUnit orgUnit : campaignSnap.getOrgUnitList() ) {
				for( Section section : campaignSnap.getDsSections() ) {
					String key1 = orgUnit.getId()+"_"+section.getCode();
					List<CampaignVO> vaccineResultList = campaignSnap.getCtDataMap().get( key1 );
					if( vaccineResultList != null ) {
						for( CampaignVO cvo : vaccineResultList ){
							Map<String, GenericDataVO> cMap = cvo.getColDataMap();
							
							row = sheet.createRow( rowCount++ );			
							colCount = 0;			
							if( campaignSnap.getIsoCode() != null && campaignSnap.getIsoCode().trim().equals("ON") ) {
								cell = row.createCell( colCount++ );
								cell.setCellStyle( dataStyle );
								cell.setCellValue( orgUnit.getCode() );				
							}
							if( campaignSnap.getWhoRegion() != null && campaignSnap.getWhoRegion().trim().equals("ON") ) {
								cell = row.createCell( colCount++ );
								cell.setCellStyle( dataStyle );
								cell.setCellValue( orgUnit.getParent().getShortName() );				
							}
							if( campaignSnap.getUnicefRegion() != null && campaignSnap.getUnicefRegion().trim().equals("ON") ) {
								cell = row.createCell( colCount++ );
								cell.setCellStyle( dataStyle );
								String tempVal = " ";
								try { tempVal = orgUnit.getGroupInGroupSet( campaignSnap.getUnicefRegionsGroupSet() ).getShortName(); }catch(Exception e) {}
								cell.setCellValue( tempVal );										
							}

							cell = row.createCell( colCount++ );
							cell.setCellStyle( dataStyle );
							cell.setCellValue( orgUnit.getShortName() );				
							
							if( campaignSnap.getIncomeLevel() != null && campaignSnap.getIncomeLevel().trim().equals("ON") ) {
								cell = row.createCell( colCount++ );
								cell.setCellStyle( dataStyle );
								String tempVal = " ";
								try { tempVal = campaignSnap.getCountryGeneralInfoMap().get(orgUnit.getId()+":3"); }catch(Exception e) {}
								cell.setCellValue( tempVal );				
							}
							if( campaignSnap.getGaviEligibleStatus() != null && campaignSnap.getGaviEligibleStatus().trim().equals("ON") ) {
								cell = row.createCell( colCount++ );
								cell.setCellStyle( dataStyle );
								String tempVal = " ";
								try { tempVal = campaignSnap.getCountryGeneralInfoMap().get(orgUnit.getId()+":4"); }catch(Exception e) {}
								cell.setCellValue( tempVal );				
							}

							cell = row.createCell( colCount++ );
							cell.setCellStyle( dataStyle );
							cell.setCellValue( section.getCode() );				
							
							String subNatName = " ";
                    		try{ subNatName = cMap.get( "COL_0" ).getStrVal1(); }catch(Exception e){}
                    		if( subNatName.trim().equals("NONE") )
                    			subNatName = " ";
							cell = row.createCell( colCount++ );
							cell.setCellStyle( dataStyle );
							cell.setCellValue( subNatName );
							
							for( GenericTypeObj colObj : campaignSnap.getColList() ){
								String comment = " ";
                        		try{ comment = cMap.get( colObj.getCode() ).getStrVal2();} catch(Exception e) {}
                            	String value = " ";
                            	try{ value = cMap.get( colObj.getCode() ).getStrVal1(); } catch(Exception e) {}
                            	if( colObj.getCode().equals("COL_3") && value.equals("May postpone") )
									value = "Might postpone";
								
                            	if( colObj.getCode().equals("COL_4") )
                            		value = comment;
                            	
								cell = row.createCell( colCount++ );
								cell.setCellStyle( dataStyle );
								cell.setCellValue( value );
								
								if( campaignSnap.getShowComment() != null && campaignSnap.getShowComment().equals("ON") && !colObj.getCode().equals("COL_3") && !colObj.getCode().equals("COL_4") && !colObj.getCode().equals("COL_13") ) {
									cell = row.createCell( colCount++ );
									cell.setCellStyle( dataStyle );
									cell.setCellValue( comment );
								}
							}
							
						}
					}
				}
			}
    	}
    	catch(Exception e) {
    		System.out.println("Error while preparing workbook object in generateCampaignTrackerXl "+ e.getMessage() );
    		e.printStackTrace();
    	}
    	
    	return workbook;
    }
}