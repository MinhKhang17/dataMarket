package com.example.datasetapi.config.system;

import com.example.datasetapi.model.Permission;
import com.example.datasetapi.model.Role;
import com.example.datasetapi.repository.PermissionRepository;
import com.example.datasetapi.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Tạo Permission nếu chưa có
   Permission readPerm = addPermission("Read");
   Permission writePerm = addPermission("Write");

    //tao permission cho user
   List<Permission> permissionsUser = Arrays.asList(readPerm, writePerm);
        //tao permission cho admin
   List<Permission> permissionsAdmin = Arrays.asList(readPerm, writePerm);
    //tao role cho admin
        addRole("ADMIN",permissionsAdmin);

    //Tao Role cho User
        addRole("USER",permissionsUser);

        System.out.println("Roles & permissions initialized.");
    }

    private Permission addPermission(String permissionName) {
        Optional<Permission> readPerm = permissionRepository.findByName(permissionName);
        Permission permission = new Permission(permissionName);
        if(!(readPerm.isPresent())) {
            try{
                permissionRepository.save(permission);
                return permission;
            }
            catch (Exception e) {
                System.out.println("can not add permission "+ permissionName);
            }
        }
        else{
            return readPerm.get();
        }

        return permission;
    }

    private void addRole(String roleName, List<Permission> permissions) {

        Optional<Role> adminRole = roleRepository.findByName(roleName);
                if(adminRole.isPresent()) {
                    return;
                }
                else{
                    Role role = new Role();
                    role.setName(roleName);
                    Set<Permission> perms = new HashSet<>();
                    for(Permission p : permissions) {
                        perms.add(p);
                    }
                    role.setPermissions(perms);
                    roleRepository.save(role);
                }
    }
}
