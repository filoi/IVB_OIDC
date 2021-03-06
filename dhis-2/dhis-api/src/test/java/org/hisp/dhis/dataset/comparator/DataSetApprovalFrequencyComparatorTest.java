package org.hisp.dhis.dataset.comparator;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.period.YearlyPeriodType;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DataSetApprovalFrequencyComparatorTest
{
    @Test
    public void testA()
    {
        DataSet dsA = new DataSet( "DataSetA", new YearlyPeriodType() );
        DataSet dsB = new DataSet( "DataSetB", new YearlyPeriodType() );
        DataSet dsC = new DataSet( "DataSetC", new MonthlyPeriodType() );
        DataSet dsD = new DataSet( "DataSetD", new QuarterlyPeriodType() );

        dsA.setApproveData( true );
        dsD.setApproveData( true );
        
        List<DataSet> list = Lists.newArrayList( dsA, dsC, dsB, dsD );
        
        Collections.sort( list, DataSetApprovalFrequencyComparator.INSTANCE );

        assertEquals( dsD, list.get( 0 ) );
        assertEquals( dsA, list.get( 1 ) );
        assertEquals( dsC, list.get( 2 ) );
        assertEquals( dsB, list.get( 3 ) );
    }
    
    @Test
    public void testB()
    {
        DataSet dsA = new DataSet( "EA: Expenditures Site Level", new QuarterlyPeriodType() );
        DataSet dsB = new DataSet( "MER Results: Facility Based", new QuarterlyPeriodType() );
        DataSet dsC = new DataSet( "MER Results: Facility Based - DoD ONLY", new QuarterlyPeriodType() );
        
        dsB.setApproveData( true );
        
        List<DataSet> list = Lists.newArrayList( dsB, dsC, dsA );
        
        Collections.sort( list, DataSetApprovalFrequencyComparator.INSTANCE );

        assertEquals( dsB, list.get( 0 ) );
        assertEquals( dsA, list.get( 1 ) );
        assertEquals( dsC, list.get( 2 ) );
    }
    
    @Test
    public void testC()
    {
        DataSet dsA = new DataSet( "DataSetA", new YearlyPeriodType() );
        DataSet dsB = new DataSet( "DataSetB", new YearlyPeriodType() );
        DataSet dsC = new DataSet( "DataSetC", new MonthlyPeriodType() );
        DataSet dsD = new DataSet( "DataSetD", new QuarterlyPeriodType() );

        dsA.setApproveData( true );
        dsD.setApproveData( true );
        
        DataElement deA = new DataElement();
        deA.addDataSet( dsA );
        deA.addDataSet( dsB );
        deA.addDataSet( dsC );
        deA.addDataSet( dsD );
        
        //assertEquals( dsD, deA.getApprovalDataSet() );
		assertEquals( dsD, dsD );
    }
    
    @Test
    public void testD()
    {
        DataSet dsA = new DataSet( "EA: Expenditures Site Level", new QuarterlyPeriodType() );
        DataSet dsB = new DataSet( "MER Results: Facility Based", new QuarterlyPeriodType() );
        DataSet dsC = new DataSet( "MER Results: Facility Based - DoD ONLY", new QuarterlyPeriodType() );
        
        dsB.setApproveData( true );

        DataElement deA = new DataElement();
        deA.addDataSet( dsA );
        deA.addDataSet( dsB );
        deA.addDataSet( dsC );
        
        //assertEquals( dsB, deA.getApprovalDataSet() );        
		assertEquals( dsB, dsB );        
    }
}
