package com.carol.customshop.service;

import com.carol.customshop.dto.*;
import com.carol.customshop.entity.*;
import com.carol.customshop.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductNotAllowedCombinationRepository productNotAllowedCombinationRepository;
    private final ProductNACombinationOverrideRepository productNACombinationOverrideRepository;
    private final ProductAttributeOverrideRepository productAttributeOverrideRepository;
    private final ProductOptionOverrideRepository productOptionOverrideRepository;
    private final ProductTypeService productTypeService;
    private final FileStorageServiceImpl fileStorageService;

    @Value("${product.images.base-path}")
    private String baseImagePath;


    public ProductService(
            ProductRepository productRepository,
            ProductNotAllowedCombinationRepository productNotAllowedCombinationRepository,
            ProductNACombinationOverrideRepository productNACombinationOverrideRepository,
            ProductAttributeOverrideRepository productAttributeOverrideRepository,
            ProductOptionOverrideRepository productOptionOverrideRepository,
            ProductTypeService productTypeService,
            FileStorageServiceImpl fileStorageService
    ) {
        this.productRepository = productRepository;
        this.productAttributeOverrideRepository = productAttributeOverrideRepository;
        this.productOptionOverrideRepository = productOptionOverrideRepository;
        this.productNACombinationOverrideRepository = productNACombinationOverrideRepository;
        this.productNotAllowedCombinationRepository = productNotAllowedCombinationRepository;
        this.productTypeService = productTypeService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ProductCreationResponse createProduct(
            String name, String sku, String description, Float price, UUID productTypeId,
            MultipartFile mainPicture, List<MultipartFile> imageGallery,
            ProductAttributeOverrides attributeOverrides, NotAllowedCombinationsOverrides nACombinationsOverrides,
            List<List<NotAllowedCombinationItem>> productNotAllowedCombinations) {

        // Validate Product Type
        ProductType productType = productTypeService.getProductTypeById(productTypeId.toString());
        if (productType == null) {
            throw new IllegalArgumentException("Invalid product type ID: " + productTypeId);
        }

        // Ensure image storage directory exists
        fileStorageService.ensureUploadDirectoryExists();

        // Store main picture
        String mainPictureUrl = null;
        if (mainPicture != null && !mainPicture.isEmpty()) {
            try {
                mainPictureUrl = fileStorageService.storeFile(mainPicture);
            } catch (IOException e) {
                log.error("Error saving main image '{}' when creating new product '{}'",
                        mainPicture.getOriginalFilename(), name, e);
            }
        }

        // Store image gallery
        List<String> imageGalleryUrls = new ArrayList<>();
        if (imageGallery != null) {
            imageGalleryUrls = imageGallery.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .map(file -> {
                        try {
                            return fileStorageService.storeFile(file);
                        } catch (IOException e) {
                            log.error("Error saving gallery image '{}' when creating new product '{}'",
                                    file.getOriginalFilename(), name, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull) // Remove failed images
                    .collect(Collectors.toList());
        }


        // Create & Save Product Entity
        Product product = new Product();
        product.setName(name);
        product.setSku(sku);
        product.setDescription(description);
        product.setPrice(price);
        product.setProductType(productType);
        product.setMainPicture(mainPictureUrl);
        product.setImageGallery(imageGalleryUrls);

        product = productRepository.save(product);

        log.info("Product successfully saved with ID: {}", product.getId());

        // 🔹 Handle Overrides (If provided)
        if (attributeOverrides != null) {
            handleProductAttributeOverrides(product, attributeOverrides);
        }

        if (nACombinationsOverrides != null) {
            handleNACombinationsOverrides(product, nACombinationsOverrides);
        }

        if (productNotAllowedCombinations != null) {
            handleProductSpecificNotAllowedCombinations(product, productNotAllowedCombinations);
        }

        // Build & Return Response
        ProductCreationResponse productCreationResponse = new ProductCreationResponse();
        productCreationResponse.setProductId(product.getId());
        productCreationResponse.setMessage("Product successfully created");
        return productCreationResponse;
    }

    @Transactional
    private void handleProductAttributeOverrides(Product product, ProductAttributeOverrides overrides) {
        List<ProductAttributeOverride> attributeOverrideEntities = new ArrayList<>();
        List<ProductOptionOverride> optionOverrideEntities = new ArrayList<>();

        // Handle Attribute Deactivation Overrides
        if (overrides.getDeactivatedAttributes() != null) {
            for (DeactivatedAttribute deactivatedAttr : overrides.getDeactivatedAttributes()) {
                ProductTypeAttribute attribute =
                        productTypeService.getProductTypeAttributeById(deactivatedAttr.getAttributeId());

                ProductAttributeOverride attributeOverride = new ProductAttributeOverride();
                attributeOverride.setProduct(product);
                attributeOverride.setAttribute(attribute);
                attributeOverride.setActive(false); // Mark as deactivated
                ProductAttributeOverride attrOverride = productAttributeOverrideRepository.save(attributeOverride);
                attributeOverrideEntities.add(attrOverride);
            }
        }

        // Handle Option Deactivation Overrides
        if (overrides.getDeactivatedOptions() != null) {
            for (DeactivatedOption deactivatedOpt : overrides.getDeactivatedOptions()) {
                ProductTypeAttributeOption option =
                        productTypeService.getProductTypeAttributeOptionById(deactivatedOpt.getOptionId());

                ProductOptionOverride optionOverride = new ProductOptionOverride();
                optionOverride.setProduct(product);
                optionOverride.setOption(option);
                optionOverride.setActive(false); // Mark as deactivated
                ProductOptionOverride opOverride = productOptionOverrideRepository.save(optionOverride);

                optionOverrideEntities.add(opOverride);
            }
        }

        // Handle Out of Stock Overrides
        if (overrides.getOutOfStockOptions() != null) {
            for (DeactivatedOption outOfStockOpt : overrides.getOutOfStockOptions()) {
                ProductTypeAttributeOption option =
                        productTypeService.getProductTypeAttributeOptionById(outOfStockOpt.getOptionId());

                // Check for an existing override in the database
                Optional<ProductOptionOverride> existingOverrideOpt =
                        productOptionOverrideRepository.findByProductAndOption(product, option);

                if (existingOverrideOpt.isPresent()) {
                    ProductOptionOverride existingOverride = existingOverrideOpt.get();
                    existingOverride.setOutOfStock(true);
                    productOptionOverrideRepository.save(existingOverride);
                } else {
                    ProductOptionOverride newOverride = new ProductOptionOverride();
                    newOverride.setProduct(product);
                    newOverride.setOption(option);
                    newOverride.setActive(true); // Keep it active but mark as out of stock
                    newOverride.setOutOfStock(true);
                    productOptionOverrideRepository.save(newOverride);
                }
            }
        }

        // Save overrides
        if (!attributeOverrideEntities.isEmpty()) {
            productAttributeOverrideRepository.saveAll(attributeOverrideEntities);
        }

        if (!optionOverrideEntities.isEmpty()) {
            productOptionOverrideRepository.saveAll(optionOverrideEntities);
        }

        // Update product entity with new overrides
        product.getAttributeOverrides().addAll(attributeOverrideEntities);
    }

    @Transactional
    private void handleNACombinationsOverrides(
            Product product,
            NotAllowedCombinationsOverrides nACombinationsOverrides)
    {
        List<ProductNACombinationOverride> deactivatedOverrides = new ArrayList<>();

        for (DeactivateCombination deactivateCombination : nACombinationsOverrides.getDeactivate()) {
            // Fetch NotAllowedCombination
            NotAllowedCombination notAllowedCombination =
                    productTypeService.getNotAllowedCombinationById(deactivateCombination.getCombinationId());

            if (notAllowedCombination == null) {
                log.warn("Skipping deactivation: NotAllowedCombination with ID {} not found",
                        deactivateCombination.getCombinationId());
                continue;
            }

            // Create and save override entry
            ProductNACombinationOverride override = new ProductNACombinationOverride();
            override.setProduct(product);
            override.setNotAllowedCombination(notAllowedCombination);
            override.setActive(false); // Mark as deactivated

            ProductNACombinationOverride savedOverride = productNACombinationOverrideRepository.save(override);
            deactivatedOverrides.add(savedOverride);
        }

        // Save all overrides if there are any
        if (!deactivatedOverrides.isEmpty()) {
            productNACombinationOverrideRepository.saveAll(deactivatedOverrides);
            product.getNotAllowedCombinationsOverrides().addAll(deactivatedOverrides);
            product = productRepository.save(product); // 🔹 Ensure the product is updated
        }
    }


    @Transactional
    private void handleProductSpecificNotAllowedCombinations(
            Product product, List<List<NotAllowedCombinationItem>> productNotAllowedCombinations) {

        List<ProductNotAllowedCombination> newCombinations = productNotAllowedCombinations.stream()
                .peek(combinationList -> {
                    if (combinationList.size() < 2) {
                        throw new IllegalArgumentException(
                                "Each product-specific not-allowed combination " +
                                        "must have at least two attribute-option pairs."
                        );
                    }
                })
                .map(combinationList -> {
                    // Save the combination first
                    ProductNotAllowedCombination combination = new ProductNotAllowedCombination();
                    combination.setProduct(product);
                    combination = productNotAllowedCombinationRepository.save(combination);

                    // Convert DTOs to entity objects AFTER combination is persisted
                    ProductNotAllowedCombination finalCombination = combination;
                    List<ProductNotAllowedCombinationElement> options = combinationList.stream()
                            .map(optionDto -> {
                                ProductTypeAttribute attribute =
                                        productTypeService.getProductTypeAttributeById(optionDto.getAttributeId());
                                if (attribute == null) {
                                    throw new IllegalArgumentException("Product Attribute not found with ID: "
                                            + optionDto.getAttributeId());
                                }

                                ProductTypeAttributeOption option =
                                        productTypeService.getProductTypeAttributeOptionById(
                                                optionDto.getAttributeOptionId()
                                        );

                                if (option == null) {
                                    throw new IllegalArgumentException("Product Attribute Option not found with ID: "
                                            + optionDto.getAttributeOptionId());
                                }

                                // Assign combination to elements so combination_id is not null
                                ProductNotAllowedCombinationElement element =
                                        new ProductNotAllowedCombinationElement(attribute, option);
                                element.setCombination(finalCombination);
                                return element;
                            })
                            .collect(Collectors.toList());

                    combination.setOptions(options);
                    return combination;
                })
                .collect(Collectors.toList());

        // Save combinations and their elements
        productNotAllowedCombinationRepository.saveAll(newCombinations);

        // Update product entity with new combinations
        product.getNotAllowedCombinations().addAll(newCombinations);
    }

    public ProductDetailsResponse getProductDetails(UUID productId) {
        // Fetch the product entity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // Construct the response DTO
        ProductDetailsResponse response = new ProductDetailsResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());

        // Use the base image path from properties
        response.setMainPicture(product.getMainPicture() != null ? getBaseUrl() + product.getMainPicture() : null);

        if (product.getImageGallery() != null) {
            List<String> imageUrls = product.getImageGallery().stream()
                    .map(fileName -> getBaseUrl() + fileName)
                    .collect(Collectors.toList());
            response.setImageGallery(imageUrls);
        }

        // Populate Product Type Details
        ProductType productType = product.getProductType();
        ProductTypeProduct productTypeResponse = new ProductTypeProduct();
        productTypeResponse.setId(productType.getId());
        productTypeResponse.setName(productType.getName());
        productTypeResponse.setConfig(
                new ProductTypeProductConfig().customisation(productType.getConfig().getCustomisation())
        );
        response.setProductType(productTypeResponse);

        // Populate Product Type Attributes & Options (Considering Overrides)
        List<AttributeResponseProduct> attributes = buildAttributeResponses(productType, product);
        response.setProductAttributes(attributes);

        // Populate Not Allowed Combinations from Product Type
        List<NotAllowedCombinationResponse> notAllowedCombinations = buildNotAllowedCombinations(productType, product);
        response.setProductNotAllowedCombinations(notAllowedCombinations);

        // Populate Product-Specific Not Allowed Combinations
        List<ProductNotAllowedCombinationResponse> productSpecificCombinations =
                buildProductSpecificNotAllowedCombinations(product);
        response.setSpecificNotAllowedCombinations(productSpecificCombinations);

        return response;
    }

    // Helper method to build Attribute Responses (Considering Overrides)
    private List<AttributeResponseProduct> buildAttributeResponses(ProductType productType, Product product) {
        List<ProductAttributeOverride> attributeOverrides = product.getAttributeOverrides();
        List<ProductOptionOverride> optionOverrides = product.getOptionOverrides();

        return productType.getAttributes().stream()
                .map(attr -> {
                    AttributeResponseProduct attrResponse = new AttributeResponseProduct();
                    attrResponse.setId(attr.getId());
                    attrResponse.setName(attr.getAttributeName());

                    // Attribute is active if it is NOT in the override table
                    boolean isAttributeActive = attributeOverrides.stream()
                            .noneMatch(override -> override.getAttribute().getId().equals(attr.getId()));

                    attrResponse.setActive(isAttributeActive);

                    // Map options while checking overrides
                    List<AttributeOptionResponse> options = attr.getOptions().stream()
                            .map(opt -> {
                                AttributeOptionResponse optionResponse = new AttributeOptionResponse();
                                optionResponse.setId(opt.getId());
                                optionResponse.setName(opt.getName());

                                // Find the override for this option, if it exists
                                ProductOptionOverride optionOverride = optionOverrides.stream()
                                        .filter(override -> override.getOption().getId().equals(opt.getId()))
                                        .findFirst()
                                        .orElse(null);

                                // If there is NO override, assume the option is active and in stock
                                if (optionOverride == null) {
                                    optionResponse.setActive(true);
                                    optionResponse.setOutOfStock(false);
                                } else {
                                    optionResponse.setActive(optionOverride.isActive());
                                    optionResponse.setOutOfStock(optionOverride.isOutOfStock());
                                }

                                return optionResponse;
                            })
                            .collect(Collectors.toList());

                    attrResponse.setOptions(options);
                    return attrResponse;
                })
                .collect(Collectors.toList());
    }


    private List<NotAllowedCombinationResponse> buildNotAllowedCombinations(ProductType productType, Product product) {
        List<ProductNACombinationOverride> combinationOverrides = product.getNotAllowedCombinationsOverrides();

        return productType.getNotAllowedCombinations().stream()
                .map(comb -> {
                    NotAllowedCombinationResponse combResponse = new NotAllowedCombinationResponse();
                    combResponse.setCombinationId(comb.getId());

                    // Check if an override exists for this combination
                    ProductNACombinationOverride override = combinationOverrides.stream()
                            .filter(ovr -> ovr.getNotAllowedCombination().getId().equals(comb.getId()))
                            .findFirst()
                            .orElse(null);

                    // If no override exists, assume the combination is active
                    combResponse.setActive(override == null || override.isActive());

                    // Map combination options using setter methods
                    List<NotAllowedCombinationOptionResponse> options = comb.getOptions().stream()
                            .map(option -> {
                                NotAllowedCombinationOptionResponse optionResponse =
                                        new NotAllowedCombinationOptionResponse();
                                optionResponse.setAttributeId(option.getAttribute().getId());
                                optionResponse.setAttributeOptionId(option.getAttributeOption().getId());
                                return optionResponse;
                            })
                            .collect(Collectors.toList());

                    combResponse.setOptions(options);
                    return combResponse;
                })
                .collect(Collectors.toList());
    }


    private List<ProductNotAllowedCombinationResponse> buildProductSpecificNotAllowedCombinations(Product product) {
        return product.getNotAllowedCombinations().stream()
                .map(comb -> {
                    ProductNotAllowedCombinationResponse combResponse = new ProductNotAllowedCombinationResponse();
                    combResponse.setCombinationId(comb.getId());

                    List<NotAllowedCombinationOptionResponse> options = comb.getOptions().stream()
                            .map(option -> {
                                NotAllowedCombinationOptionResponse optionResponse =
                                        new NotAllowedCombinationOptionResponse();
                                optionResponse.setAttributeId(option.getAttribute().getId());
                                optionResponse.setAttributeOptionId(option.getOption().getId());
                                return optionResponse;
                            })
                            .collect(Collectors.toList());

                    combResponse.setOptions(options);
                    return combResponse;
                })
                .collect(Collectors.toList());
    }
    private String getBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
    }

    @Transactional
    public AvailableAttributeOptionsResponse getAvailableOptionsBySelection(
            UUID productId, Long requestedAttributeId, List<Long> selectedOptionIds
    ) {
        // Validate Product Existence
        Product product = getProductById(productId);

        // Validate Requested Attribute Existence
        ProductTypeAttribute requestedAttribute = productTypeService.getProductTypeAttributeById(requestedAttributeId);
        List<ProductTypeAttributeOption> allOptions = requestedAttribute.getOptions();

        // Validate Selected Options
        validateSelectedOptions(selectedOptionIds, requestedAttributeId);

        // Fetch Product-Specific Overrides
        Set<Long> deactivatedAttributes = getDeactivatedAttributes(product);
        Set<Long> deactivatedOptions = getDeactivatedOptions(product);
        Set<Long> outOfStockOptions = getOutOfStockOptions(product);

        // Fetch Not-Allowed Combinations
        Set<Long> forbiddenOptions = new HashSet<>();

        // Fetch general product-type-based not-allowed combinations
        forbiddenOptions.addAll(getForbiddenOptionsFromCombinations(
                product.getProductType(), selectedOptionIds, requestedAttributeId)
        );

        // Fetch product-specific not-allowed combinations
        forbiddenOptions.addAll(getForbiddenOptionsFromProductCombinations(
                product, selectedOptionIds, requestedAttributeId)
        );

        // Filter Allowed Options
        List<AttributeOption> availableOptions = allOptions.stream()
                .filter(option -> !deactivatedAttributes.contains(requestedAttributeId)) // Ensure attribute is active
                .filter(option -> !deactivatedOptions.contains(option.getId())) // Ensure option is active
                .filter(option -> !forbiddenOptions.contains(option.getId())) // Ensure option is allowed
                .filter(option -> !outOfStockOptions.contains(option.getId())) // Ensure is not out of stock
                .map(option -> {
                            AttributeOption opt = new AttributeOption();
                            opt.setId(option.getId());
                            opt.setName(option.getName());
                            return opt;
                        }
                )
                .collect(Collectors.toList());

        // Return Response DTO
        return new AvailableAttributeOptionsResponse(
                requestedAttribute.getId(),
                requestedAttribute.getAttributeName(),
                availableOptions
        );
    }

    private void validateSelectedOptions(List<Long> selectedOptionIds, Long requestedAttributeId) {
        Map<Long, Long> selectedAttributesMap = new HashMap<>();

        for (Long optionId : selectedOptionIds) {
            ProductTypeAttributeOption option = productTypeService.getProductTypeAttributeOptionById(optionId);
            if (option == null) {
                throw new IllegalArgumentException("Invalid option ID: " + optionId);
            }

            Long attributeId = option.getAttribute().getId();

            // Ensure that each attribute has only one selected option
            if (selectedAttributesMap.containsValue(attributeId)) {
                throw new IllegalArgumentException("Multiple options selected for the same attribute: " + attributeId);
            }

            // Ensure no selected option belongs to the requested attribute
            if (attributeId.equals(requestedAttributeId)) {
                throw new IllegalArgumentException("Selected option belongs to the requested attribute: " + optionId);
            }

            selectedAttributesMap.put(optionId, attributeId);
        }
    }

    private Product getProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
    }

    private Set<Long> getDeactivatedAttributes(Product product) {
        return productAttributeOverrideRepository.findByProductAndActiveFalse(product)
                .stream()
                .map(override -> override.getAttribute().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getDeactivatedOptions(Product product) {
        return productOptionOverrideRepository.findByProductAndActiveFalse(product)
                .stream()
                .map(override -> override.getOption().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getOutOfStockOptions(Product product) {
        return productOptionOverrideRepository.findByProductAndOutOfStockTrue(product)
                .stream()
                .map(override -> override.getOption().getId())
                .collect(Collectors.toSet());
    }

    private Set<Long> getForbiddenOptionsFromCombinations(
            ProductType productType, List<Long> selectedOptionIds, Long requestedAttributeId
    ) {
        Set<Long> forbiddenOptions = new HashSet<>();
        List<NotAllowedCombination> combinations = productType.getNotAllowedCombinations();

        for (NotAllowedCombination combination : combinations) {
            List<NotAllowedCombinationElement> elements = combination.getOptions();

            // Extract the set of options in the forbidden combination
            Set<Long> forbiddenSet = elements.stream()
                    .map(element -> element.getAttributeOption().getId())
                    .collect(Collectors.toSet());

            // Ensure the forbidden set contains an option for the requested attribute
            Optional<Long> forbiddenOptionForRequestedAttribute = elements.stream()
                    .filter(element -> element.getAttribute().getId().equals(requestedAttributeId))
                    .map(element -> element.getAttributeOption().getId())
                    .findFirst();

            if (forbiddenOptionForRequestedAttribute.isPresent()) {
                Long forbiddenOption = forbiddenOptionForRequestedAttribute.get();

                // Check if selecting this option would complete a forbidden combination
                Set<Long> selectedSet = new HashSet<>(selectedOptionIds);
                selectedSet.add(forbiddenOption); // Simulate selecting this option

                if (selectedSet.containsAll(forbiddenSet)) {
                    forbiddenOptions.add(forbiddenOption);
                }
            }
        }
        return forbiddenOptions;
    }


    private Set<Long> getForbiddenOptionsFromProductCombinations(
            Product product, List<Long> selectedOptionIds, Long requestedAttributeId) {

        Set<Long> forbiddenOptions = new HashSet<>();
        List<ProductNotAllowedCombination> combinations = productNotAllowedCombinationRepository.findByProduct(product);

        for (ProductNotAllowedCombination combination : combinations) {
            List<ProductNotAllowedCombinationElement> elements = combination.getOptions();

            // Extract the set of options in the forbidden combination
            Set<Long> forbiddenSet = elements.stream()
                    .map(element -> element.getOption().getId())
                    .collect(Collectors.toSet());

            // Ensure the forbidden set contains an option for the requested attribute
            Optional<Long> forbiddenOptionForRequestedAttribute = elements.stream()
                    .filter(element -> element.getAttribute().getId().equals(requestedAttributeId))
                    .map(element -> element.getOption().getId())
                    .findFirst();

            if (forbiddenOptionForRequestedAttribute.isPresent()) {
                Long forbiddenOption = forbiddenOptionForRequestedAttribute.get();

                // Check if selecting this option would complete a forbidden combination
                Set<Long> selectedSet = new HashSet<>(selectedOptionIds);
                selectedSet.add(forbiddenOption); // Simulate selecting this option

                if (selectedSet.containsAll(forbiddenSet)) {
                    forbiddenOptions.add(forbiddenOption);
                }
            }
        }
        return forbiddenOptions;
    }
}
