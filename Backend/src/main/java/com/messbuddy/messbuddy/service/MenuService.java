package com.messbuddy.messbuddy.service;

import com.messbuddy.messbuddy.entity.Menu;
import com.messbuddy.messbuddy.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public Menu createMenu(String ownerId, Menu request) {
        Menu menu = Menu.builder()
                .Menu_Name(request.getMenu_Name())
                .Description(request.getDescription())
                .Price(request.getPrice())
                .Owner_ID(ownerId)
                .Availability(request.getAvailability() == null ? "Yes" : request.getAvailability())
                .Food_Type(request.getFood_Type() == null ? "Veg" : request.getFood_Type())
                .Date(request.getDate() == null ? LocalDateTime.now() : request.getDate())
                .build();
        return menuRepository.save(menu);
    }

    public List<Menu> getAllMenus(String ownerId) {
        return menuRepository.findByOwnerId(ownerId);
    }

    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    public List<Menu> searchMenu(String ownerId, String query) {
        String search = query == null ? "" : query.toLowerCase();
        return menuRepository.findByOwnerId(ownerId).stream()
                .filter(menu -> menu.getMenu_Name() != null && menu.getMenu_Name().toLowerCase().contains(search))
                .toList();
    }

    public Menu updateMenu(String menuId, Menu request) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found"));
        menu.setMenu_Name(request.getMenu_Name());
        menu.setDescription(request.getDescription());
        menu.setPrice(request.getPrice());
        menu.setAvailability(request.getAvailability());
        menu.setFood_Type(request.getFood_Type());
        return menuRepository.save(menu);
    }

    public void deleteMenu(String menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu not found"));
        menuRepository.delete(menu);
    }
}
