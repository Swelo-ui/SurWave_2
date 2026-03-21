import os
from PIL import Image, ImageDraw

def create_circular_mask(h, w):
    mask = Image.new('L', (w, h), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, w, h), fill=255)
    return mask

def generate_icons(source_path, base_dir):
    sizes = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    img = Image.open(source_path).convert("RGBA")
    
    for dpi, size in sizes.items():
        folder = os.path.join(base_dir, f"mipmap-{dpi}")
        if not os.path.exists(folder):
            os.makedirs(folder)
            
        # Resize image
        resized = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save ic_launcher.webp and ic_launcher_static.png
        # Some WebP conversions might lose transparency if not handled, but PIL handles RGBA WebP
        resized.save(os.path.join(folder, "ic_launcher.webp"), format="WEBP", lossless=True)
        resized.save(os.path.join(folder, "ic_launcher_static.png"), format="PNG")
        
        # Create round mask
        round_img = Image.new("RGBA", (size, size))
        mask = create_circular_mask(size, size)
        round_img.paste(resized, (0, 0), mask=mask)
        
        # Save ic_launcher_round.webp
        round_img.save(os.path.join(folder, "ic_launcher_round.webp"), format="WEBP", lossless=True)

if __name__ == "__main__":
    source_img = r"C:\Users\DELL\.gemini\antigravity\brain\d0e9e52b-597e-4b34-82d4-d4ebf66ffd90\surwave_app_icon_1774072466701.png"
    base_res_dir = r"h:\SurWave2\app\src\main\res"
    generate_icons(source_img, base_res_dir)
    print("Icons generated successfully!")
