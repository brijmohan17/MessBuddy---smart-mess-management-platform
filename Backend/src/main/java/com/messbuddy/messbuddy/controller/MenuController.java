package com.messbuddy.messbuddy.controller;

import com.messbuddy.messbuddy.entity.Menu;
import com.messbuddy.messbuddy.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

	private final MenuService menuService;

	@PostMapping("/create/{ownerId}")
	public ResponseEntity<?> createMenu(@PathVariable String ownerId, @RequestBody Menu request) {
		Menu menu = menuService.createMenu(ownerId, request);
		return ResponseEntity.status(201).body(Map.of("success", true, "message", "Menu created successfully", "menu", menu));
	}

	@GetMapping("/{ownerId}")
	public ResponseEntity<?> getAllMenus(@PathVariable String ownerId) {
		return ResponseEntity.ok(Map.of("success", true, "menus", menuService.getAllMenus(ownerId)));
	}

	@GetMapping
	public ResponseEntity<?> getAllMenus() {
		return ResponseEntity.ok(Map.of("success", true, "menus", menuService.getAllMenus()));
	}

	@PutMapping("/update/{menuId}")
	public ResponseEntity<?> updateMenu(@PathVariable String menuId, @RequestBody Menu request) {
		return ResponseEntity.ok(Map.of("success", true, "message", "Menu updated successfully", "menu", menuService.updateMenu(menuId, request)));
	}

	@DeleteMapping("/delete/{menuId}")
	public ResponseEntity<?> deleteMenu(@PathVariable String menuId) {
		menuService.deleteMenu(menuId);
		return ResponseEntity.ok(Map.of("success", true, "message", "Menu deleted successfully"));
	}

	@GetMapping("/search/{ownerId}")
	public ResponseEntity<?> searchMenu(@PathVariable String ownerId, @RequestParam(required = false) String query) {
		return ResponseEntity.ok(Map.of("success", true, "menus", menuService.searchMenu(ownerId, query)));
	}
}
