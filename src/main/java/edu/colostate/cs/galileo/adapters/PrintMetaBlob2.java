package edu.colostate.cs.galileo.adapters;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import edu.colostate.cs.galileo.dataset.Metadata;
import edu.colostate.cs.galileo.serialization.SerializationInputStream;
import edu.colostate.cs.galileo.serialization.Serializer;
import edu.colostate.cs.galileo.util.Geohash;
import edu.colostate.cs.galileo.dataset.feature.Feature;

public class PrintMetaBlob2 {

    public static final String[] attribs = {
        "geopotential_height_lltw",
        "water_equiv_of_accum_snow_depth_surface",
        "drag_coefficient_surface",
        "sensible_heat_net_flux_surface",
        "categorical_ice_pellets_yes1_no0_surface",
        "visibility_surface",
        "number_of_soil_layers_in_root_zone_surface",
        "categorical_freezing_rain_yes1_no0_surface",
        "pressure_reduced_to_msl_msl",
        "upward_short_wave_rad_flux_surface",
        "relative_humidity_zerodegc_isotherm",
        "categorical_snow_yes1_no0_surface",
        "u-component_of_wind_tropopause",
        "surface_wind_gust_surface",
        "total_cloud_cover_entire_atmosphere",
        "upward_long_wave_rad_flux_surface",
        "land_cover_land1_sea0_surface",
        "vegitation_type_as_in_sib_surface",
        "v-component_of_wind_pblri",
        "albedo_surface",
        "lightning_surface",
        "ice_cover_ice1_no_ice0_surface",
        "convective_inhibition_surface",
        "pressure_surface",
        "transpiration_stress-onset_soil_moisture_surface",
        "soil_porosity_surface",
        "vegetation_surface",
        "categorical_rain_yes1_no0_surface",
        "downward_long_wave_rad_flux_surface",
        "planetary_boundary_layer_height_surface",
        "soil_type_as_in_zobler_surface",
        "geopotential_height_cloud_base",
        "friction_velocity_surface",
        "maximumcomposite_radar_reflectivity_entire_atmosphere",
        "plant_canopy_surface_water_surface",
        "v-component_of_wind_maximum_wind",
        "geopotential_height_zerodegc_isotherm",
        "mean_sea_level_pressure_nam_model_reduction_msl",
        "temperature_surface",
        "snow_cover_surface",
        "geopotential_height_surface",
        "convective_available_potential_energy_surface",
        "latent_heat_net_flux_surface",
        "surface_roughness_surface",
        "pressure_maximum_wind",
        "temperature_tropopause",
        "geopotential_height_pblri",
        "pressure_tropopause",
        "snow_depth_surface",
        "v-component_of_wind_tropopause",
        "downward_short_wave_rad_flux_surface",
        "u-component_of_wind_maximum_wind",
        "wilting_point_surface",
        "precipitable_water_entire_atmosphere",
        "u-component_of_wind_pblri",
        "direct_evaporation_cease_soil_moisture_surface",
    };

    public static void main(String args[]) throws Exception {
        String fileName = args[0];
//        System.out.println("Reading metadata blob: " + fileName);
        FileInputStream fIn = new FileInputStream(fileName);
        BufferedInputStream bIn = new BufferedInputStream(fIn);
        SerializationInputStream in = new SerializationInputStream(bIn);

        int num = in.readInt();
//        System.out.println("Reading " + num);
        for (int i = 0; i < num; ++i) {
            /* Just ignore the lat/lon header: */
            in.readFloat();
            in.readFloat();

            byte[] metaBytes = in.readField();
            Metadata meta = Serializer.deserialize(
                    Metadata.class, metaBytes);
            System.out.print(meta.getTemporalProperties().getEnd() + "\t");
            System.out.print(
                    Geohash.encode(
                        meta.getSpatialProperties().getCoordinates(), 12));
            System.out.print("\t");
            for (String attrib : attribs) {
                Feature f = meta.getAttribute(attrib);
                if (f == null) {
                    System.err.println("Missing attribute: " + attrib);
                    System.out.print("[null]" + "\t");
                    continue;
                }
                System.out.print(f.getString() + "\t");
            }
            System.out.println();
        }

        in.close();
    }
}
