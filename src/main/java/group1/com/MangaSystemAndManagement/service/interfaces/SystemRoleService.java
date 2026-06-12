package group1.com.MangaSystemAndManagement.service.interfaces;


import group1.com.MangaSystemAndManagement.model.SystemRole;
import java.util.List;
import java.util.Optional;

public interface SystemRoleService {

        SystemRole create(group1.com.MangaSystemAndManagement.dto.request.SystemRoleRequest request);

        Optional<SystemRole> findById(Long id);

        List<SystemRole> findAll();

        SystemRole update(Long id, group1.com.MangaSystemAndManagement.dto.request.SystemRoleRequest request);

        void delete(Long id);

        Optional<SystemRole> findByName(String name);

}
