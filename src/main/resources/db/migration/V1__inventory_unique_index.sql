ALTER TABLE inventory
    ADD UNIQUE INDEX uq_inventory_material_storage_type (material_id, storage_id, type);
