package com.example.Structures;

public class Plant {
        private Teracota.PlantTypes plantType;
        private int growDays;
        public Plant(Teracota.PlantTypes pt, int growDays)
        {
            plantType = pt;
            this.growDays = growDays;
        }
        public Teracota.PlantTypes getPlantTypes() {return plantType;}
        public int getGrowDays() {return growDays;}
}
