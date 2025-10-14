    package com.example.datasetapi.model.location;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.Id;
    import jakarta.persistence.Table;
    import lombok.Data;

    @Entity
    @Table(name = "provinces")
    @Data
    public class Province {

        @Id
        @Column(name = "id_province")
        private String idProvince;

        private String name;
    }
