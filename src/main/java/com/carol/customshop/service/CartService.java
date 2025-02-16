package com.carol.customshop.service;

import com.carol.customshop.dto.*;
import com.carol.customshop.entity.CartItem;
import com.carol.customshop.entity.Product;
import com.carol.customshop.entity.ProductTypeAttributeOption;
import com.carol.customshop.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    ProductService productService;
    CartRepository cartRepository;

    public CartService(ProductService productService, CartRepository cartRepository) {
        this.productService = productService;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public CartItemResponse addItemToCart(AddCartItemRequest request) {
        // Validate User Existence - pending

        // Validate Product Existence
        Product product = productService.getProductById(request.getProductId());

        // Validate Selected Options
        List<ProductTypeAttributeOption> selectedOptions =
                validateAndRetrieveOptions(product, request.getSelectedOptions());

        // Generate Label for Display
        String optionNames = selectedOptions.stream()
                .map(ProductTypeAttributeOption::getName)
                .collect(Collectors.joining(", "));

        String itemLabel = String.format("%s (%s)", product.getName(), optionNames);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(request.getQuantity());
        cartItem.setSelectedOptions(selectedOptions);
        cartItem.setLabel(itemLabel);

        cartItem = cartRepository.save(cartItem);

        return buildCartItemResponse(cartItem);
    }

    private List<ProductTypeAttributeOption> validateAndRetrieveOptions(
            Product product,
            List<AddCartItemRequestSelectedOptionsInner> selectedOptions
    ) {
        Set<Long> productAttributeIds = productService.getValidAttributeIdsForProduct(product.getId());

        List<ProductTypeAttributeOption> validatedOptions = new ArrayList<>();

        for (AddCartItemRequestSelectedOptionsInner option : selectedOptions) {
            if (!productAttributeIds.contains(option.getAttributeId())) {
                throw new IllegalArgumentException("Invalid attribute for this product: " + option.getAttributeId());
            }
            ProductTypeAttributeOption productOption = productService.getOptionById(option.getOptionId());
            validatedOptions.add(productOption);
        }

        return validatedOptions;
    }

    private CartItemResponse buildCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setCartItemId(cartItem.getId());
        response.setProductId(cartItem.getProduct().getId());
        response.setProductName(cartItem.getProduct().getName());
        response.setQuantity(cartItem.getQuantity());
        response.setLabel(cartItem.getLabel());

        List<CartItemResponseSelectedOptionsInner> selectedOptions = cartItem.getSelectedOptions().stream()
                .map(option -> {
                    CartItemResponseSelectedOptionsInner optionResponse = new CartItemResponseSelectedOptionsInner();
                    optionResponse.setAttributeId(option.getAttribute().getId());
                    optionResponse.setOptionId(option.getId());
                    return optionResponse;
                })
                .collect(Collectors.toList());

        response.setSelectedOptions(selectedOptions);
        return response;
    }

    public CartResponse getCartItems() {
        List<CartItem> cartItems = getLast10CartItems();

        // Convert entities to DTOs
        List<CartItemResponse> responseList = cartItems.stream()
                .map(cartItem -> {
                    CartItemResponse response = new CartItemResponse();
                    response.setCartItemId(cartItem.getId());
                    response.setProductId(cartItem.getProduct().getId());
                    response.setProductName(cartItem.getProduct().getName());
                    response.setProductImage(productService.getProductImage(cartItem.getProduct().getId()));
                    response.setQuantity(cartItem.getQuantity());
                    response.setLabel(cartItem.getLabel());

                    // Map selected options
                    List<CartItemResponseSelectedOptionsInner> selectedOptions = cartItem.getSelectedOptions().stream()
                            .map(option -> {
                                CartItemResponseSelectedOptionsInner optionResponse =
                                        new CartItemResponseSelectedOptionsInner();
                                optionResponse.setAttributeId(option.getAttribute().getId());
                                optionResponse.setOptionId(option.getId());
                                return optionResponse;
                            })
                            .collect(Collectors.toList());

                    response.setSelectedOptions(selectedOptions);
                    return response;
                })
                .collect(Collectors.toList());

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItems(responseList);
        return cartResponse;
    }

    public List<CartItem> getLast10CartItems() {
        Page<CartItem> cartItemsPage = cartRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return cartItemsPage.getContent();
    }
}
