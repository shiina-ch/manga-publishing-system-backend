package group1.com.MangaSystemAndManagement.service.interfaces;


import group1.com.MangaSystemAndManagement.model.SystemRole;
import java.util.List;
import java.util.Optional;

public interface SystemRoleService {

        SystemRole create(SystemRole systemRole);

        Optional<SystemRole> findById(Long id);

        List<SystemRole> findAll();

        SystemRole update(Long id, SystemRole systemRole);

        void delete(Long id);

        Optional<SystemRole> findByName(String name);

}
