//
//  Created by Aleksey Mikhailov on 23/06/2019.
//  Copyright © 2019 IceRock Development. All rights reserved.
//

import UIKit
import MultiPlatformLibrary

class TestViewController: UIViewController {
    
    @IBOutlet var imageView: UIImageView!
    @IBOutlet var label: UILabel!
    
    var viewModel: ImageSelectionViewModel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let permissionsController = PermissionsController()
        let mediaPickerController = MediaPickerController(
            permissionsController: permissionsController,
            viewController: self
        )
        viewModel = ImageSelectionViewModel(
            mediaPickerController: mediaPickerController
        )
        
        viewModel.selectedImage.addObserver { [weak self] image in
            self?.imageView.image = image?.image
        }
        viewModel.textState.addObserver { [weak self] text in
            self?.label.text = text as String?
        }
    }
    
    @IBAction func onCameraPressed() {
        viewModel.onCameraPressed()
    }

    @IBAction func onGalleryPressed() {
        viewModel.onGalleryPressed()
    }
    
    @IBAction private func onPickFileTapped() {
        viewModel.onFilePressed()
    }
}
