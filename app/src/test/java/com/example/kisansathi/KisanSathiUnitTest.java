package com.example.kisansathi;

import com.example.kisansathi.models.CropGuide;
import com.example.kisansathi.models.MandiPrice;
import com.example.kisansathi.models.Product;
import com.example.kisansathi.models.User;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KisanSathiUnitTest {

    @Test
    public void testProductUnitPriceCalculationSolid() {
        Product product = new Product("p1", "Wheat Seed", "Seeds", "AgriCorp",
                "10 kg", 10.0, "kg", 850.0, "2026-10-22",
                "Sow in rows", "Keep dry", true);

        assertEquals(85.0, product.getUnitPrice(), 0.001);
        assertEquals("₹85.00 / kg", product.getFormattedUnitPrice());
    }

    @Test
    public void testProductUnitPriceCalculationLiquid() {
        Product liquidProduct = new Product("p10", "Chlorpyrifos 20% EC Insecticide", "Crop Protection Products", "CropCare",
                "1 L", 1.0, "L", 480.0, "2026-10-22",
                "Dilute 2 ml/L water", "Wear protective gear", true);

        assertEquals(480.0, liquidProduct.getUnitPrice(), 0.001);
        assertEquals("₹480.00 / L", liquidProduct.getFormattedUnitPrice());
    }

    @Test
    public void testProductInvalidQuantityUnitPrice() {
        Product productZero = new Product("p2", "Zero Pack", "Seeds", "AgriCorp",
                "0 kg", 0.0, "kg", 100.0, "2026-10-22",
                "N/A", "N/A", true);

        assertEquals(0.0, productZero.getUnitPrice(), 0.001);
        assertEquals("N/A", productZero.getFormattedUnitPrice());
    }

    @Test
    public void testUserModel() {
        User user = new User("Ramesh Kumar", "ramesh@example.com");

        assertEquals("Ramesh Kumar", user.getName());
        assertEquals("ramesh@example.com", user.getEmail());

        user.setName("Suresh Kumar");
        assertEquals("Suresh Kumar", user.getName());
    }

    @Test
    public void testMandiPriceModel() {
        MandiPrice mandi = new MandiPrice("m1", "Wheat", "Punjab", "Ludhiana", "Khanna Mandi",
                2125.0, 2350.0, 2275.0, "Qtl", "2026-10-22", "Punjab Agmarknet", true);

        assertEquals("Wheat", mandi.getCropName());
        assertEquals("Punjab", mandi.getState());
        assertEquals(2275.0, mandi.getModalPrice(), 0.001);
        assertTrue(mandi.isSampleData());
    }

    @Test
    public void testCropGuideModelWithFertilizerGuidance() {
        CropGuide crop = new CropGuide("c1", "Wheat", "Rabi", "Loamy Soil",
                "Plough and sow 40 kg/acre", "Irrigate 4-6 times", "Apply DAP at sowing & split Urea",
                "Harvest when golden", true);

        assertNotNull(crop.getCropId());
        assertEquals("Rabi", crop.getSeason());
        assertEquals("Loamy Soil", crop.getSoil());
        assertEquals("Apply DAP at sowing & split Urea", crop.getFertilizerGuidance());
        assertTrue(crop.isSampleData());
    }
}
