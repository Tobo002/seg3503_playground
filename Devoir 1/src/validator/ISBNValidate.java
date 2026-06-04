/*
 * @(#)ISBNValidate.java
 *
 * Summary: Library of methods to validate and format ISBNs (International Standard Book Numbers).
 *
 * Copyright: (c) 1999-2011 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.5+
 *
 * Created with: IntelliJ IDEA IDE.
 *
 * Version History:
 *  2.3 2009-03-16 tidy code, rename some methods.
 */
package validator;

import java.util.BitSet;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Library of methods to validate and format ISBNs (International Standard Book Numbers).
 * <p/>
 * Typical valid ISBNs look like this:
 * group/country-publisher-title#-checkdigit
 * examples of good formatted
 * ISBNs 0-00-000000-0 0-200-00000-4 0-7000-0000-3
 * 0-85000-000-9 0-900000-00-7 0-9500000-0-0 0-9999999-9-0 1-00-000000-1 1-09-999999-5 1-100-00000-3 1-399-99999-0
 * 1-4000-0000-9 1-4999-9999-2 1-5000-0000-0 1-5499-9999-0 1-55000-000-4 1-86979-999-2 1-869800-00-1 1-9990000-0-5
 * 2-00000000-2 2-95000000-2 3-00000000-3 3-99999999-3 4-00000000-4 4-95000000-4 5-00000000-5 5-95000000-5 7-00000000-7
 * 7-20000000-0 7-95000000-7 80-0000000-8 93-9999999-8 950-000000-8 950-999999-7 9949-99999-5 9950-00000-9 9959-99999-8
 * 9960-00000-1 9989-99999-6 99901-9601-X
 * <p/>
 * examples of bad ISBNs 6000000006 6950000006 6999999996 9400000006 9419000001
 * 949999999X 992999999X 9930000003 9934000008 9939999992 9940000006 9999900003 9999999999
 * The lead 0 is significant. They always contain 10 digits, possibly with the last check digit character being X.
 * <p/>
 * ISBN-13, for now always begin 978- and use a different check digit mechanism. It has a numeric checkdigit.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 2.3 2009-03-16 tidy code, rename some methods.
 * @since 1999
 */
public final class ISBNValidate
    {
    // ------------------------------ CONSTANTS ------------------------------

    private static final boolean DEBUGGING = false;

    /**
     * define bandd of IBSN numbers formatted differently to format an ISBN, after categorising it into a range of numbers.
     * Number in table is  high+1 for the band.
     * Even illegal bands get formatted in some way
     */
    private static final int[] BANDS = {
            // --------0 0-00-bbbbbb-x group 0 00.......19 1st 3rd 9th digit
            +20000000,
            // --------1 0-200-bbbbb-x 200......699 " 4th "
            +70000000,
            // --------2 0-7000-bbbb-x 7000.....8499 " 5th "
            +85000000,
            // --------3 0-85000-bbb-x 85000....89999 " 6th "
            +90000000,
            // --------4 0-900000-bb-x 900000...949999 " 7th "
            +95000000,
            // --------5 0-9500000-b-x 9500000..9999999 " 8th "
            100000000,
            // --------6 1-00-bbbbbb-x group 1 00.......09 1st 3rd 9th digit
            110000000,
            // --------7 1-100-bbbbb-x 100......399 " 4th "
            140000000,
            // --------8 1-4000-bbbb-x 4000.....5499 " 5th "
            155000000,
            // --------9 1-55000-bbb-x 55000....86979 1st 6th 9th digit
            186980000,
            // -------10 1-869800-bb-x 869800...998999 " 7th "
            199900000,
            // -------11 1-1999000-b-x 9990000..9999999 " 8th "
            200000000,
            // -------12 2-rrrrrrrr-x groups 2 .. 7 1st 9th digit
            800000000,
            // -------13 80-rrrrrrr-x groups 80 .. 94 2nd 9th digit
            950000000,
            // -------14 950-rrrrrr-x groups 950 .. 993 3rd 9th digit
            994000000,
            // -------15 9940-rrrrr-x groups 9940 .. 9989 4th 9th digit
            999000000,
            // -------16 groups 99900 .. 99999 5th 9th digit
            1000000000 };

    /**
     * List of legal groups The following table gives the range remainder distribution of the group identifiers: for
     * pub+title 0 - 7 9 80 - 94 8 950 - 993 7 9940 - 9989 6 99900 - 99999 5 Group is the part of the number before the
     * first dash. as of 2005-06-19 only used to build bit map lookup see legal list at:
     * http://www.isbn-international.org/en/identifiers/allidentifiers.html
     * <p/>
     * We convert he list to a bit map indexed by the first 5 digits to say whether an ISBN-10 could start with those 5
     * digits.  For 13-digit ISBNs, we may eventually need different group tables for different 3-digit prefixes.
     */
    private static final int[] VALID_GROUPS = {
            0,
            1,
            2,
            3,
            4,
            5,
            7,
            80,
            81,
            82,
            83,
            84,
            85,
            86,
            87,
            88,
            89,
            90,
            91,
            92,
            93,
            98,
            600,
            950,
            951,
            952,
            953,
            954,
            955,
            956,
            957,
            958,
            959,
            960,
            961,
            962,
            963,
            964,
            965,
            966,
            967,
            968,
            969,
            970,
            971,
            972,
            973,
            974,
            975,
            976,
            977,
            978,
            979,
            980,
            981,
            982,
            983,
            984,
            985,
            986,
            987,
            988,
            989,
            9940,
            9941,
            9942,
            9943,
            9944,
            9945,
            9946,
            9947,
            9948,
            9949,
            9950,
            9951,
            9952,
            9953,
            9954,
            9955,
            9956,
            9957,
            9958,
            9959,
            9960,
            9961,
            9962,
            9963,
            9964,
            9965,
            9966,
            9967,
            9968,
            9970,
            9971,
            9972,
            9973,
            9974,
            9975,
            9976,
            9977,
            9978,
            9979,
            9980,
            9981,
            9982,
            9983,
            9984,
            9985,
            9986,
            9987,
            9988,
            9989,
            99901,
            99902,
            99903,
            99905,
            99906,
            99908,
            99909,
            99910,
            99911,
            99912,
            99913,
            99914,
            99915,
            99916,
            99917,
            99918,
            99919,
            99920,
            99921,
            99922,
            99923,
            99924,
            99925,
            99926,
            99927,
            99928,
            99929,
            99930,
            99931,
            99932,
            99933,
            99934,
            99935,
            99936,
            99937,
            99938,
            99939,
            99940,
            99941,
            99942,
            99943,
            99944,
            99945,
            99946,
            99947,
            99948,
            99949,
            99950,
            99951,
            99952,
            99953,
            99954,
            99955 };

    /**
     * bit map of all valid groups, indexed by first 5 digits of ISBN currently all groups are valid, index into it to
     * see if that group is valid.
     */
    private static BitSet groupValidLookup;
    // -------------------------- PUBLIC STATIC METHODS --------------------------

    /**
     * Calculate the expected ISBN-13 checkdigit from the first 12 digits. inserts hyphens.
     *
     * @param dashlessISBN12 first 12 digits expressed as String. May not contain any extra lead/trail chars.
     *
     * @return complete ISBN with checkdigit without dashes..
     */
    @SuppressWarnings( { "WeakerAccess" } )
    public static String appendCheckDigitToISBN12( String dashlessISBN12 )
    {
    if ( dashlessISBN12.length() != 12 )
        {
        throw new IllegalArgumentException(
                "ISBN-12 must be 12 chars long without dashes, without check digit" );
        }
    return dashlessISBN12 + calcISBN13CheckDigit( dashlessISBN12 );
    }// end appendCheckDigitToISBN12

    /**
     * Calculate the expected ISBN-10 checkdigit from the first 12 digits. inserts hyphens.
     *
     * @param dashlessISBN9 first 9 digits expressed as String. May not contain any extra lead/trail chars.
     *
     * @return complete ISBN-10 with checkdigit without dashes.
     */
    @SuppressWarnings( { "WeakerAccess" } )
    public static String appendCheckDigitToISBN9( String dashlessISBN9 )
    {
    if ( dashlessISBN9.length() != 9 )
        {
        throw new IllegalArgumentException(
                "ISBN-9 must be 9 chars long without dashes, without check digit" );
        }
    return dashlessISBN9 + calcISBN10CheckDigit( dashlessISBN9 );
    }// end appendCheckDigitToISBN9

    /**
     * Checks if ISBN-10 is valid.
     *
     * @param dashlessISBN10 isbn, no dashes, 10 chars long, 0-9 possibly last char X.
     */
    @SuppressWarnings( { "WeakerAccess" } )
    public static void ensureISBN10Valid( String dashlessISBN10 )
    {
    if ( dashlessISBN10.length() != 10 )
        {
        throw new IllegalArgumentException( "ISBN-10 must be 10 digits long" );
        }
    if ( dashlessISBN10.charAt( 9 ) == 'x' )
        {
        throw new IllegalArgumentException( "ISBN-10 should end in a capital X" );
        }
    ensureISBN10GroupValid( dashlessISBN10 );
    ensureISBN10PublisherValid( dashlessISBN10 );
    if ( !isISBN10CheckDigitValid( dashlessISBN10 ) )
        {
        throw new IllegalArgumentException( "Bad Check Digit" );
        }
    }

    /**
     * Checks if ISBN-13 is valid.
     *
     * @param dashlessISBN13 isbn, no dashes, 13 chars long
     */
    @SuppressWarnings( { "WeakerAccess" } )
    public static void ensureISBN13Valid( String dashlessISBN13 )
    {
    if ( dashlessISBN13.length() != 13 )
        {
        throw new IllegalArgumentException( "ISBN-13 must be 13 digits." );
        }
    final String lead = dashlessISBN13.substring( 0, 3 );
    if ( !( lead.equals( "978" ) || lead.equals( "979" ) ) )
        {
        throw new IllegalArgumentException(
                "ISBN-13 must begin with 978 or 979." );
        }
    final String isbn10Part = dashlessISBN13.substring( 3, 13 );
    ensureISBN10GroupValid( isbn10Part );
    // ensure publisher is ok
    ensureISBN10PublisherValid( isbn10Part );
    // ensure check digit ok
    if ( !isISBN13CheckDigitValid( dashlessISBN13 ) )
        {
        throw new IllegalArgumentException( "Bad check digit." );
        }
    }

    /**
     * Validates checkdigit for an ISBN-10
     *
     * @param dashlessISBN10 isbn, no dashes, 10 chars long, 0-9 possibly last char X.
     *
     * @return true if check digit is correct.
     *         <p/>
     *         see <a href="http://www.isbn.org/standards/home/isbn/international/html/usm4.htm">isbn.org</a> for
     *         details. Weights are 10 9 8 7 6 5 4 3 2 1 mod 11, X represents modulo 10. sum mod 11 must be 0.
     */
    @SuppressWarnings( { "BooleanMethodIsAlwaysInverted", "WeakerAccess" } )
    public static boolean isISBN10CheckDigitValid( String dashlessISBN10 )
    {
    if ( dashlessISBN10.length() != 10 )
        {
        throw new IllegalArgumentException(
                "ISBN-10 must be 10 digits long." );
        }
    return ( calcISBN10CheckDigit( dashlessISBN10.substring( 0, 9 ) )
             == dashlessISBN10.charAt( 9 ) );
    }// end isISBN10CheckDigitValid

    /**
     * Validates checkdigit on ISBN-13
     *
     * @param dashlessISBN13 isbn, no dashes, 13chars long, 0-9
     *
     * @return true if check digit is correct.
     *         <p/>
     *         see <a href="http://www.isbn.org/standards/home/isbn/international/html/usm4.htm">isbn.org</a> for
     *         details. Weights are 10 9 8 7 6 5 4 3 2 1 mod 11, X represents modulo 10. sum mod 11 must be 0.
     */
    @SuppressWarnings( { "BooleanMethodIsAlwaysInverted", "WeakerAccess" } )
    public static boolean isISBN13CheckDigitValid( String dashlessISBN13 )
    {
    if ( dashlessISBN13.length() != 13 )
        {
        throw new IllegalArgumentException(
                "ISBN-13 must be 13 digits long." );
        }
    return ( calcISBN13CheckDigit( dashlessISBN13.substring( 0, 12 ) )
             == dashlessISBN13.charAt( 12 ) );
    }// end isISBN13CheckDigitValid

    /**
     * converts old ISBN-10 to ISBN-13 form
     *
     * @param dashlessISBN10 ISBN:10 without dashes, with check digit.
     *
     * @return complete ISBN-13 with checkdigit without dashes.
     */
    public static String isbn10To13( String dashlessISBN10 )
    {
    if ( dashlessISBN10.length() != 10 )
        {
        throw new IllegalArgumentException(
                "ISBN-10 must be 10 chars long without dashes, with check digit" );
        }
    return appendCheckDigitToISBN12( "978" + dashlessISBN10.substring( 0,
            9 ) );
    }

    /**
     * converts new ISBN-13 to old ISBN-10 form
     *
     * @param dashlessISBN13 ISBN:13 without dashes, with check digit.
     *
     * @return complete ISBN-10 with checkdigit without dashes. Returns "n/a" if there is no equivalent ISBN-10 code.
     */
    public static String isbn13To10( String dashlessISBN13 )
    {
    if ( dashlessISBN13.length() != 13 )
        {
        throw new IllegalArgumentException(
                "ISBN-13 must be 13 chars long without dashes, with check digit." );
        }
    if ( dashlessISBN13.substring( 0, 3 ).equals( "978" ) )
        {
        return appendCheckDigitToISBN9( dashlessISBN13.substring( 3, 12 ) );
        }
    else
        {
        return ( "n/a" );
        }
    }

    /**
     * Tidies up an ISBN by first removing stray dashes and
     * non-numerics. Validates checksum, checks publisher number, and inserts dashes where they belong. Discards
     * whatever it finds before or after the ISBN.
     *
     * @param rawISBN10or13 rawISBN text to be tidied, possibly with extra dashes, spaces or other junk chars.
     *
     * @return tidied dashed ISBN with lead and trail junk discarded. or an error message if the ISBN is invalid.
     */
    public static String tidyISBN10or13InsertingDashes( String rawISBN10or13 )
    {
    if ( rawISBN10or13 == null || rawISBN10or13.length() == 0 )
        {
        throw new IllegalArgumentException( "no ISBN" );
        }
    // strip lead, trail and embedded junk including dashes
    final String dashlessISBN = strip( rawISBN10or13 );
    final int length = dashlessISBN.length();
    switch ( length )
        {
        case 10:
            ensureISBN10Valid( dashlessISBN );
            return insertDashesInISBN10or13( dashlessISBN );
        case 13:
            ensureISBN13Valid( dashlessISBN );
            return insertDashesInISBN10or13( dashlessISBN );
        default:
            throw new IllegalArgumentException(
                    "ISBN must be 10 or 13 digits long." );
        }
    }// end tidyISBN10or13InsertingDashes

    /**
     * TOP LEVEL method, used by Amazon book macro to get ISBN without dashes.. Tidies up an ISBN by removing all dashes
     * and non-numerics. Validates checksum, checks publisher number, Discards whatever it finds before or after the
     * ISBN e.g.
     *
     * @param rawISBN10or13 rawISBN text to be tidied, may contain spaces, dashes or other junk chars.
     *
     * @return tidied dash-free ISBN with lead and trail junk discarded. or an error message if the ISBN is invalid.
     */
    public static String tidyISBN10or13RemovingDashes( String rawISBN10or13 )
    {
    if ( rawISBN10or13 == null || rawISBN10or13.length() == 0 )
        {
        throw new IllegalArgumentException( "no ISBN" );
        }
    // strip lead, trail and embedded junk including dashes
    final String dashlessISBN = strip( rawISBN10or13 );
    final int length = dashlessISBN.length();
    switch ( length )
        {
        case 10:
            ensureISBN10Valid( dashlessISBN );
            return dashlessISBN;
        case 13:
            ensureISBN13Valid( dashlessISBN );
            return dashlessISBN;
        default:
            throw new IllegalArgumentException(
                    "must be 10 or 13 digits long." );
        }
    }// end tidyISBN10or13RemovingDashes

    // -------------------------- STATIC METHODS --------------------------

    static
        {
        buildGroupValidLookup();
        }

    /**
     * Build a BitSet to check if a group is valid, static initialisation
     */
    private static void buildGroupValidLookup()
    {
    groupValidLookup = new BitSet( 100000 );
    for ( int group : VALID_GROUPS )
        {
        // how many digits wide
        int width = Integer.toString( group ).length();
        switch ( width )
            {
            case 5:
                groupValidLookup.set( group );
                break;
            case 4:
                for ( int j = group * 10; j <= group * 10 + 9; j++ )
                    {
                    groupValidLookup.set( j );
                    }
                break;
            case 3:
                for ( int j = group * 100; j <= group * 100 + 99; j++ )
                    {
                    groupValidLookup.set( j );
                    }
                break;
            case 2:
                for ( int j = group * 1000; j <= group * 1000 + 999; j++ )
                    {
                    groupValidLookup.set( j );
                    }
                break;
            case 1:
                for ( int j = group * 10000; j
                                             <= group * 10000 + 9999; j++ )
                    {
                    groupValidLookup.set( j );
                    }
                break;
            default:
                throw new IllegalArgumentException(
                        "bug: bad VALID_GROUPS table" );
            }// end switch
        }// end for
    }

    /**
     * Calculate the expected ISBN-10 checkdigit from the first 9 digits.
     *
     * @param dashlessISBN9 first 9 digits expressed as String. May not contain any extra lead/trail chars.
     *
     * @return just the check digit as a char.
     */
    private static char calcISBN10CheckDigit( String dashlessISBN9 )
    {
    if ( dashlessISBN9.length() != 9 )
        {
        throw new IllegalArgumentException( "ISBN-9 must be 9 digits." );
        }
    int sum = 0;
    /*         0 1 2 3 4 5 6 7 8|9
    ISBN       9 9 9 0 1 9 6 0 1|X
    Weighting 10 9 8 7 6 5 4 3 2|1
    Values   90 + 81 + 72 + 0 + 6 + 45 + 24 + 0 + 2  = 320  mod 11 = 1
    divide by 11 and get remainder. If 0, checkdigit is 0
    if 1 the checkdigit is X,
    otherwise it is 10 - remainder.
    */
    // not your usual odd-even. work right to left,  slot 8, 7 ... 0
    for ( int weight = 2; weight <= 10; weight++ )
        {
        // works even though 10th digit not there yet
        int digit = dashlessISBN9.charAt( 10 - weight ) - '0';
        sum += digit * weight;
        }// end for
    final int remainder = sum % 11;
    // sum 0->0, 1->10(X), 2->9. 3->8, 4->7, 5->6, 6->5, 7->4, 8->3, 9->2,
    // 10->1
    switch ( remainder )
        {
        case 0:
            return '0';
        case 1:
            return 'X';
        default:
            return ( char ) ( 11 - remainder + '0' );
        }
    }// end calcISBN10CheckDigit

    /**
     * Calculate the expected checkdigit for an ISBN-13. http://www.isbn.org/standards/home/isbn/transition.asp uses
     * mod-10 checkdigit with weight 3.
     *
     * @param dashlessISBN12 first 12 digits expressed as String. May not contain any extra lead/trail chars.
     *
     * @return just the check digit as a char. always a digit '0' to '9'
     */
    private static char calcISBN13CheckDigit( String dashlessISBN12 )
    {
    /*        0 1 2 3 4 5 6 7 8 9 A B|C
    ISBN      9 7 8 0 9 4 0 0 1 6 7 3|6
    Weighting 1 3 1 3 1 3 1 3 1 3 1 3|1
    Values    9 + 21 + 8 + 0 + 9 + 12 + 0 + 0 + 1 + 18 + 7 + 9 = 94  mod 10 = 4, 10 - 4 = 6
    divide by 10 and get remainder. If 0, checkdigit is 0, otherwise it is 10 - remainder.
    */
    if ( dashlessISBN12.length() != 12 )
        {
        throw new IllegalArgumentException( "ISBN-12 must be 12 digits." );
        }
    int sum = 0;
    // work left to right, start with weight 1, alternating weight 3
    for ( int i = 0; i < 12; i++ )
        {
        // extract digit
        final int digit = dashlessISBN12.charAt( i ) - '0';
        // even cols weight 1, odd cols weight 3
        sum += ( i & 1 ) != 0 ? digit * 3 : digit;
        }
    final int remainder = sum % 10;
    return ( remainder == 0 ) ? '0' : ( char ) ( '0' + 10 - remainder );
    }// end calcISBN13CheckDigit

    /**
     * Some group numbers are invalid.
     *
     * @param isbn10 isbn, no dashes, 10 chars long, 0-9 possibly last char X.
     */
    @SuppressWarnings( { "BooleanMethodIsAlwaysInverted" } )
    private static void ensureISBN10GroupValid( String isbn10 )
    {
    // first 5 digits
    int group;
    try
        {
        group = Integer.parseInt( isbn10.substring( 0, 5 ) );
        }
    catch ( NumberFormatException e )
        {
        throw new IllegalArgumentException( "Bad group number" );
        }
    if ( !groupValidLookup.get( group ) )
        {
        throw new IllegalArgumentException( "Bad group number" );
        }
    }// end isGroupValid

    /**
     * Some publisher numbers could be invalid. They would be too long to leave room for a book number.
     *
     * @param dashlessISBN10 isbn, no dashes, 10 chars long, 0-9 possibly last char X.
     */
    @SuppressWarnings( { "BooleanMethodIsAlwaysInverted" } )
    private static void ensureISBN10PublisherValid( String dashlessISBN10 )
    {
    try
        {
        Integer.parseInt( dashlessISBN10.substring( 0, 9 ) );
        }
    catch ( NumberFormatException e )
        {
        throw new IllegalArgumentException( "Bad publisher" );
        }
    // The no known invalid publisher numbers.
    // There are none in group 0 & 1.
    // in other groups I don't know.
    }

    /**
     * Inserts dashes between group, publisher, title and checkdigit.
     * see http://isbn.org/standards/home/isbn/international/hyphenation-instructions.asp
     * For correct presentation, the 10 digits of an ISBN must be divided, by hyphens, into four parts:
     * <p/>
     * Part 1: The country or group of countries identifier
     * <p/>
     * Part 2: The publisher identifier
     * <p/>
     * Part 3: The title identifier
     * <p/>
     * Part 4: The check digit Group
     * <p/>
     * Where to put the dashes depends on the range of number. There are 12 different patterns. for ISBN 10.
     * We format the last 10 digits of an ISBN-13 the same way we would an ISBN-10 with a lead 978-.
     * Common usage is to drop the dashes for ISBN-13. People find them too complicated to do properly.
     * We don't because it makes them easier to read.
     *
     * @param dashlessISBN10or13 isbn, no dashes, 10 chars long, 0-9 possibly last char X. must be a valid ISBN number.
     *
     * @return ISBN with dashed inserted.
     */
    private static String insertDashesInISBN10or13( String dashlessISBN10or13 )
    {
    // first 9 digits
    int range;
    boolean isISBN13;
    final int length = dashlessISBN10or13.length();
    try
        {
        switch ( length )
            {
            case 10:
                range =
                        Integer.parseInt( dashlessISBN10or13.substring( 0,
                                9 ) );
                isISBN13 = false;
                break;
            case 13:
                range =
                        Integer.parseInt( dashlessISBN10or13.substring( 3,
                                12 ) );
                isISBN13 = true;
                break;
            default:
                return dashlessISBN10or13;
            }
        }
    catch ( NumberFormatException e )
        {
        return dashlessISBN10or13;
        }
    // 0-based where first dash goes
    int whereFirstDash = -1;
    // 0-based where middle dash goes, -1 = none
    int whereMidDash = -1;
    // never varies since always single-digit check digit.
    // 0-based where last dash goes
    final int whereLastDash = 8;
    boolean tryAgain = false;
    do
        {
        // figure out where middle dash goes
        int band = 0;
        for ( int i = 0; i < BANDS.length; i++ )
            {
            if ( range < BANDS[ i ] )
                {
                band = i;
                break;
                }// end if
            }// end for
        switch ( band )
            {
            // 1st 3rd 9th digit
            case 0:
                /* publisher 00 .. 19 : 0-00-bbbbbb-x */
            case 6:
                /* publisher 10 .. 09 : 1-00-bbbbbb-x */
                whereFirstDash = 0;
                whereMidDash = whereFirstDash + 2;
                break;
            // 1st 4th 9th digit
            case 1:
                /* publisher 200 .. 699 : 0-200-bbbbb-x */
            case 7:
                /* publisher 100.. 399 : 1-100-bbbbb-x */
                whereFirstDash = 0;
                whereMidDash = whereFirstDash + 3;
                break;
            // 1st 5th 9th digit
            case 2:
                /* publisher 7000 .. 8499 : 0-7000-bbbb-x */
            case 8:
                /* publisher 4000.. 5499 : 1-4000-bbbb-x */
                whereFirstDash = 0;
                whereMidDash = whereFirstDash + 4;
                break;
            // 1st 6th 9th digit
            case 3:
                /* publisher 85000 .. 89999 : 0-85000-bbb-x */
            case 9:
                /* publisher 55000 .. 86979 : 0-54999-bbb-x */
                whereFirstDash = 0;
                whereMidDash = whereFirstDash + 5;
                break;
            // 1st 7th 9th digit
            case 4:
                /* publisher 900000 .. 94999 : 0-90000-bb-x */
            case 10:
                /* publisher 869800 .. 998999 : 0-869800-bb-x */
                whereFirstDash = 0;
                whereMidDash = whereFirstDash + 6;
                break;
            // 1st 8th 9th digit
            case 5:
                /* publisher 9500000 .. 9999999 : 0-950000-b-x */
            case 11:
                /* publisher 9990000 .. 9999999 : 0-9990000-b-x */
                whereFirstDash = 0;
                whereMidDash = whereFirstDash + 7;
                break;
            // 1st 9th digit
            case 12:/* group codes 2 .. 7 : 2-rrrrrrrr-x */
                whereFirstDash = 0;
                /* leave out dash between publisher and title */
                break;
            // 2nd 9th digit
            case 13:/* group codes 80 .. 94: 80-rrrrrrr-x */
                whereFirstDash = 1;
                /* leave out dash between publisher and title */
                break;
            // 3rd 9th digit
            case 14:/* group codes 950..993 : 930-rrrrrr-x */
                whereFirstDash = 2;
                /* leave out dash between publisher and title */
                break;
            // 4th 9th digit
            case 15:/* group codes 9940 .. 9989 : 9940-rrrrr-x */
                whereFirstDash = 3;
                /* leave out dash between publisher and title */
                break;
            // 5th 9th digit
            case 16:/* group codes 99900 .. 99999 : 99900-rrrr-x */
                whereFirstDash = 4;
                /* leave out dash between publisher and title */
                break;
            }// end switch
        }
    while ( tryAgain );
    // insert the three dashes
    StringBuilder cooked = new StringBuilder( 13 + 4 );
    if ( isISBN13 )
        {
        cooked.append( dashlessISBN10or13.substring( 0, 3 ) );
        cooked.append( '-' );
        }
    for ( int i = 0; i < 10; i++ )
        {
        char c = dashlessISBN10or13.charAt( isISBN13 ? i + 3 : i );
        cooked.append( c );
        if ( i == whereFirstDash
             || i == whereMidDash
             || i == whereLastDash )
            {
            cooked.append( '-' );
            }
        }// end for
    return cooked.toString();
    }// end insertDashesInISBN10or13

    /**
     * strips out extraneous characters, leaving 10 digits (possibly X for last) stores lead and trail junk around
     * number for later use by sandwich.
     *
     * @param rawISBN10or13 string with lead and trail and embedded junk, e.g. punctuation, spaces.
     *
     * @return just the digits and X, ideally 10 or 13 digits worth.
     */
    private static String strip( String rawISBN10or13 )
    {
    // find where where lead junk ends
    boolean foundNumbers = false;
    for ( int i = 0; i < rawISBN10or13.length(); i++ )
        {
        char c = rawISBN10or13.charAt( i );
        if ( "0123456789".indexOf( c ) >= 0 )
            {
            // We have found the beginning of the ISBN
            // Should really just have to look for 0, 1 or 7, but
            // user might think you can leave off lead 0.
            rawISBN10or13 = rawISBN10or13.substring( i );
            foundNumbers = true;
            break;
            }
        }// end for
    if ( !foundNumbers )
        {
        return "";
        }
    // find where where trailing junk ends
    // can't run off the end of the loop
    for ( int i = rawISBN10or13.length() - 1; i >= 0; i-- )
        {
        char c = rawISBN10or13.charAt( i );
        if ( "0123456789Xx".indexOf( c ) >= 0 )
            {
            // we have found the end of the ISBN
            rawISBN10or13 = rawISBN10or13.substring( 0, i + 1 );
            break;
            }
        }// end for
    // remove embedded junk
    StringBuilder cooked = new StringBuilder( rawISBN10or13.length() );
    for ( int i = 0; i < rawISBN10or13.length(); i++ )
        {
        char c = rawISBN10or13.charAt( i );
        if ( "0123456789".indexOf( c ) >= 0 )
            {
            cooked.append( c );
            }
        else if ( "Xx".indexOf( c ) >= 0 )
            {
            cooked.append( 'X' );
            }
        else
            {
            /* ignore */
            }
        }// end for
    if ( cooked.length() == 14 && cooked.charAt( 13 ) == 'X' )
        {
        cooked.delete( 13, 13 );
        }
    return cooked.toString();
    }// end strip

    // --------------------------- main() method ---------------------------

    /**
     * test harness
     *
     * @param args not used.
     */
    public static void main( String[] args )
    {
    if ( DEBUGGING )
        {
        String[] tests = {
                // 9 digits long. We append a good check digit before
                // testing
                "000000000",
                "039306163",
                /* 0-393-06163-9 978-0-393-06163-5 Making of the Fittest */
                "094001673",
                "157675414",
                "020000000",
                "061868000",
                /* 0-618-68000-4 978-0-618-68000-9  Dawkins */
                "070000000",
                "085000000",
                "090000000",
                "094001673",
                /* 0-940016-73-7  978-0-940016-73-6  isbn for dummies example*/
                "095000000",
                "099999999",
                "100000000",
                "109999999",
                "110000000",
                "139999999",
                "140000000",
                "149999999",
                "150000000",
                "154999999",
                "155000000",
                "186979999",
                "186980000",
                "199900000",
                "200000000",
                "295000000",
                "300000000",
                "399999999",
                "400000000",
                "495000000",
                "500000000",
                "595000000",
                "600000000",
                "700000000",
                "720000000",
                "795000000",
                "800000000",
                "939999999",
                "950000000",
                "950999999",
                "994000000",
                "994999999",
                "995000000",
                "995999999",
                "996000000",
                "998999999",
                "999019601",
                "999555555" };
        try
            {
            for ( String test : tests )
                {
                out.println( ISBNValidate.appendCheckDigitToISBN9( test ) );
                }
            for ( String test : tests )
                {
                out.println( ISBNValidate.appendCheckDigitToISBN12( "978"
                                                                    + test ) );
                }
            out.println( "isbn13To10" );
            out.println( isbn13To10( "9780618680009" ) );
            out.println( "isbn10To13" );
            out.println( isbn10To13( "0618680004" ) );
            out.println( "tidyISBN10or13InsertingDashes" );
            out.println( tidyISBN10or13InsertingDashes( "9780618680009" ) );
            out.println( tidyISBN10or13InsertingDashes( "0618680004" ) );
            out.println( "tidyISBN10or13RemovingDashes" );
            out.println( tidyISBN10or13RemovingDashes( "9780618680009" ) );
            out.println( tidyISBN10or13RemovingDashes( "0618680004" ) );
            out.println( "calcISBN13CheckDigit" );
            out.println( calcISBN13CheckDigit( "978061868000" ) );
            out.println( "calcISBN10CheckDigit" );
            out.println( calcISBN10CheckDigit( "061868000" ) );
            out.println( "isISBN13CheckDigitValid" );
            out.println( isISBN13CheckDigitValid( "9780618680009" ) );
            out.println( "isISBN10CheckDigitValid" );
            out.println( isISBN10CheckDigitValid( "0618680004" ) );
            }
        catch ( Exception e )
            {
            err.println();
            e.printStackTrace( err );
            }
        }// end if debugging
    }
    }
