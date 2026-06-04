package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import validator.ISBNValidate;


public class ISBNValidateTest {

    @Nested
    class AppendCheckDigitToISBN12Test {

        @Test
        void ct1_validTwelveDigitIsbn() {
            assertEquals(
                    "9780618680009",
                    ISBNValidate.appendCheckDigitToISBN12( "978061868000" ) );
        }

        @Test
        void ct2_minimumNumericValue() {
            assertEquals(
                    "0000000000000",
                    ISBNValidate.appendCheckDigitToISBN12( "000000000000" ) );
        }

        @Test
        void ct3_maximumNumericValue() {
            String result = ISBNValidate.appendCheckDigitToISBN12( "999999999999" );
            assertEquals( "9999999999994", result );
            assertTrue( ISBNValidate.isISBN13CheckDigitValid( result ) );
        }

        @Test
        void ct4_tooShortElevenCharacters() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.appendCheckDigitToISBN12( "97806186800" ) );
        }

        @Test
        void ct5_tooLongThirteenCharacters() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.appendCheckDigitToISBN12( "9780618680009" ) );
        }

        @Test
        void ct6_nonNumericCharacters() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.appendCheckDigitToISBN12( "97806-18-68-" ) );
        }

        @Test
        void ct7_extraCharacterBefore() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.appendCheckDigitToISBN12( " 978061868000" ) );
        }

        @Test
        void ct8_extraCharacterAfter() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.appendCheckDigitToISBN12( "978061868000 " ) );
        }

        @Test
        void ct9_belowMinimumNumericValue() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.appendCheckDigitToISBN12( "-00000000001" ) );
        }
    }

    
    @Nested
    class TidyISBN10or13InsertingDashesTest {

        // Cadre F1 — Présence : null [error]
        @Test
        void f1_nullInput() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( null ) );
        }

        // Cadre F2 — Présence : "" [error]
        @Test
        void f2_emptyInput() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "" ) );
        }

        // Cadre F3 — Longueur : autre (9) [error]
        @Test
        void f3_nineDigitsAfterStrip() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "061868000" ) );
        }

        // Cadre F4 — Longueur : autre (14) [error]
        @Test
        void f4_fourteenDigitsAfterStrip() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "97806186800099" ) );
        }

        // Cadre F5 — ISBN-10 : somme de contrôle invalide [error]
        @Test
        void f5_isbn10BadCheckDigit() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "0618680000" ) );
        }

        // Cadre F6 — ISBN-10 : groupe invalide [error]
        @Test
        void f6_isbn10BadGroup() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "6950000006" ) );
        }

        // Cadre F7 — ISBN-13 : préfixe invalide [error]
        @Test
        void f7_isbn13InvalidPrefix() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "9770618680009" ) );
        }

        // Cadre F8 — ISBN-13 : somme de contrôle invalide [error]
        @Test
        void f8_isbn13BadCheckDigit() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ISBNValidate.tidyISBN10or13InsertingDashes( "9780618680000" ) );
        }

        // Cadre F9 — ISBN-10 : contrôle X [single]
        @Test
        void f9_isbn10CheckDigitX() {
            assertEquals(
                    "99901-9601-X",
                    ISBNValidate.tidyISBN10or13InsertingDashes( "999019601X" ) );
        }

        // Cadre F10 — ISBN-13 : préfixe 979 [single]
        @Test
        void f10_isbn13Prefix979() {
            assertEquals(
                    "979-0-618-68000-8",
                    ISBNValidate.tidyISBN10or13InsertingDashes( "9790618680008" ) );
        }

        // Cadre F11 — Non vide · Len13 · sans décoration · ISBN13_978
        @Test
        void f11_isbn13Plain() {
            assertEquals(
                    "978-0-618-68000-9",
                    ISBNValidate.tidyISBN10or13InsertingDashes( "9780618680009" ) );
        }

        // Cadre F12 — Non vide · Len13 · tirets intégrés · ISBN13_978
        @Test
        void f12_isbn13WithDashes() {
            assertEquals(
                    "978-0-618-68000-9",
                    ISBNValidate.tidyISBN10or13InsertingDashes( "978-0-618-68000-9" ) );
        }

        // Cadre F13 — Non vide · Len13 · bruit tête/queue · ISBN13_978
        @Test
        void f13_leadingAndTrailingJunk() {
            assertEquals(
                    "978-0-618-68000-9",
                    ISBNValidate.tidyISBN10or13InsertingDashes( " 9780618680009 " ) );
        }

        // Cadre F14 — Non vide · Len10 · sans décoration · ISBN10OK
        @Test
        void f14_isbn10Plain() {
            assertEquals(
                    "0-618-68000-4",
                    ISBNValidate.tidyISBN10or13InsertingDashes( "0618680004" ) );
        }

        // Cadre F15 — Non vide · Len10 · tirets intégrés · ISBN10OK
        @Test
        void f15_isbn10WithDashes() {
            assertEquals(
                    "0-618-68000-4",
                    ISBNValidate.tidyISBN10or13InsertingDashes( "0-618-68000-4" ) );
        }
    }
}
